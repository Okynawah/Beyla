package main

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"net/url"
	"time"

	"github.com/bluele/gcache"
	"github.com/gocolly/colly"
	"github.com/redis/go-redis/v9"
	"github.com/temoto/robotstxt"
)

type Page struct {
	Title   string   `selector:"title"`
	Outlink []string `selector:"a[href]" attr:"href"`
	H1      []string `selector:"h1"`
	H2      []string `selector:"h2"`
	H3      []string `selector:"h3"`
	H4      []string `selector:"h4"`
	H5      []string `selector:"h5"`
	H6      []string `selector:"h6"`
	Url     string
	Text    string
}

var UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36"

func main() {
	fmt.Println("Crawler start...")
	ctx := context.Background()
	gc := gcache.New(50).LRU().Build()
	rraw := redis.NewClient(&redis.Options{
		Addr:     "localhost:26300",
		Password: "", // No password set
		DB:       0,  // Use default DB
		Protocol: 2,  // Connection protocol
	})

	rexplore := redis.NewClient(&redis.Options{
		Addr:     "localhost:26305",
		Password: "", // No password set
		DB:       0,  // Use default DB
		Protocol: 2,  // Connection protocol
	})

	rvisited := redis.NewClient(&redis.Options{
		Addr:     "localhost:26303",
		Password: "", // No password set
		DB:       0,  // Use default DB
		Protocol: 2,  // Connection protocol
	})

	routine(rexplore, rraw, rvisited, ctx, gc)

}

func routine(rexplore *redis.Client, rraw *redis.Client, rvisited *redis.Client, ctx context.Context, gc gcache.Cache) {
	for true {
		url, err := rexplore.RPop(ctx, "queue").Result()
		if err != nil {
			fmt.Print(".")
			time.Sleep(time.Second)
			continue
		}
		b := doesComplyWithRobotstxt(url, gc)
		if b {
			crawler(url, ctx, rraw, rvisited)
		}
	}
}

func crawler(url string, ctx context.Context, craw *redis.Client, cvisited *redis.Client) {

	c := colly.NewCollector()
	page := &Page{}

	c.UserAgent = UserAgent

	c.OnHTML("html", func(h *colly.HTMLElement) {
		h.Unmarshal(page)

		page.Url = url
		page.Text = h.Text
		m, _ := json.Marshal(page)
		fmt.Println("Scrapping:", page.Url)
		_, err1 := cvisited.LPush(ctx, "queue", url).Result()
		if err1 != nil {
			panic(err1)
		}
		_, err2 := craw.LPush(ctx, "queue", m).Result()
		if err2 != nil {
			panic(err2)
		}
	})
	c.Visit(url)
}

func doesComplyWithRobotstxt(urlString string, gc gcache.Cache) bool {

	parsedURL, err := url.Parse(urlString)
	if err != nil {
		log.Println("Error parsing URL:", err)
		return true
	}

	host := parsedURL.Hostname()
	if host == "" {
		log.Println("No host found in URL")
		return true
	}

	var robotBody []byte
	value, err := gc.Get(host)

	if err != nil {
		req, err := http.NewRequest("GET", parsedURL.Scheme+"://"+host+"/robots.txt", nil)
		if err != nil {
			log.Fatalln("Error creating request:", err)
			return true
		}

		client := &http.Client{}
		resp, err := client.Do(req)
		if err != nil {
			log.Fatalln("Error doing request")
			return false
		}
		defer resp.Body.Close()

		if resp.StatusCode != 200 {
			log.Print("Robot not found")
			return true // Assume no robots.txt means no specific instruction for scraping
		}

		robotBody, err = io.ReadAll(resp.Body)
		if err != nil {
			log.Println("Error reading response body:", err)
			return true
		}
		gc.Set(host, robotBody)
		log.Println("set cache")
	} else {
		robotBody = value.([]byte)
		log.Println("Cached")
	}

	robots, err := robotstxt.FromBytes(robotBody)
	if err != nil {
		log.Println("Error parsing robots.txt:", err)
		return true
	}

	if robots == nil {
		log.Println("robots is nil")
		return true
	}

	allow := robots.TestAgent(urlString, UserAgent)
	return allow
}

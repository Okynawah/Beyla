package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/url"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/joho/godotenv"
	"github.com/redis/go-redis/v9"
	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
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

type QueueOutlink struct {
	URL     string   `json:"url"`
	Outlink []string `json:"outlink"`
}

func main() {
	fmt.Println("Searching for outlinks...")
	_ = godotenv.Load("../../.local.env")
	ctx := context.Background()

	rprocessed := redis.NewClient(&redis.Options{
		Addr:     os.Getenv("BEYLA_PROCESSED") + ":" + os.Getenv("BEYLA_PROCESSED_PORT"),
		Password: "", // No password set
		DB:       0,  // Use default DB
		Protocol: 2,  // Connection protocol
	})

	rexplore := redis.NewClient(&redis.Options{
		Addr:     os.Getenv("BEYLA_EXPLORE") + ":" + os.Getenv("BEYLA_EXPLORE_PORT"),
		Password: "", // No password set
		DB:       0,  // Use default DB
		Protocol: 2,  // Connection protocol
	})

	rvisited := redis.NewClient(&redis.Options{
		Addr:     os.Getenv("BEYLA_VISITED") + ":" + os.Getenv("BEYLA_VISITED_PORT"),
		Password: "", // No password set
		DB:       0,  // Use default DB
		Protocol: 2,  // Connection protocol
	})

	mDataClient, _ := mongo.Connect(options.Client().ApplyURI("mongodb://" + os.Getenv("BEYLA_DATA") + ":" + os.Getenv("BEYLA_DATA_PORT")))

	defer func() {
		if err := mDataClient.Disconnect(ctx); err != nil {
			panic(err)
		}
	}()
	mData := mDataClient.Database("indexation").Collection("windex")
	whitelist := []string{"fr.wikipedia.org"}
	blacklist := []string{"Discussion_utilisateur", "Utilisateur:", ":Contributions", "wikipedia.org/w/index.php?title=", "wikipedia.org/wiki/Spécial:", "wikipedia.org/wiki/Sp%C3%A9cial", "wikipedia.org/wiki/Wikipédia:", "wikipedia.org/wiki/Fichier:", "wikipedia.org/wiki/Portail:", "Wikipédia:Sondage"}

	for true {
		fmt.Print(".")

		outlinksRaw, err := rprocessed.SPop(ctx, "queue").Result()
		if err != nil {
			time.Sleep(time.Second)
			continue
		}

		var queueOutlink QueueOutlink
		err = json.Unmarshal([]byte(outlinksRaw), &queueOutlink)
		if err != nil {
			fmt.Println("Erreur JSON outlink:", err, ", ", queueOutlink.URL)
			return
		}

		urlSite, err := url.Parse(queueOutlink.URL)
		if err != nil {
			fmt.Println("Erreur Parsing:", err, ", ", queueOutlink.URL)
			return
		}
		cleanedOutlinks := cleanOutlinks(queueOutlink.Outlink, urlSite.Host)
		whiteListedOutlinks := allowOutlinks(cleanedOutlinks, whitelist)
		blacklistedOutlinks := forbidOutlinks(whiteListedOutlinks, blacklist)

		fmt.Print("Found: " + strconv.Itoa(len(blacklistedOutlinks)) + " outlinks from: " + queueOutlink.URL + "\n")

		for _, outlink := range blacklistedOutlinks {
			existsVisited, err := rvisited.SIsMember(ctx, "queue", outlink).Result()
			if err != nil {
				fmt.Println("Erreur redis visited:", err)
				continue
			}
			if existsVisited == true {
				updateDataWeight(mData, outlink, ctx)
				continue
			}
			existsExplore, err := rexplore.SIsMember(ctx, "queue", outlink).Result()
			if err != nil {
				fmt.Println("Erreur redis visited:", err)
				continue
			}
			if existsExplore == true {
				updateDataWeight(mData, outlink, ctx)
				continue
			}

			_, err2 := rexplore.SAdd(ctx, "queue", outlink).Result()
			if err2 != nil {
				panic(err2)
			}
		}

	}

}

func cleanOutlinks(outlinks []string, host string) []string {
	uniqueLinks := make(map[string]bool)
	var cleanedLinks []string

	for _, outlink := range outlinks {
		outlink = strings.TrimSpace(outlink)
		if outlink == "" {
			continue
		}

		if hashIndex := strings.Index(outlink, "#"); hashIndex != -1 {
			outlink = outlink[:hashIndex]
		}

		outlink = strings.TrimSuffix(outlink, "index.html")
		outlink = strings.TrimSuffix(outlink, "/")

		if strings.HasPrefix(outlink, "/") {
			outlink = host + outlink
		}

		if !uniqueLinks[outlink] {
			uniqueLinks[outlink] = true
			cleanedLinks = append(cleanedLinks, outlink)
		}
	}

	return cleanedLinks
}

func allowOutlinks(outlinks []string, whitelist []string) []string {
	var allowedLinks []string

	for _, outlink := range outlinks {
		outlink = "https://" + outlink // TODO pas très beau
		parsedURL, err := url.Parse(outlink)
		if err != nil {
			continue
		}

		for _, allowed := range whitelist {
			allowed = strings.TrimSpace(allowed) // TODO à virer
			if outlink == allowed || parsedURL.Host == allowed {
				allowedLinks = append(allowedLinks, outlink)
				break
			}
		}
	}
	return allowedLinks

}

func forbidOutlinks(outlinks []string, blacklist []string) []string {
	var allowedLinks []string

	for _, link := range outlinks {
		allowed := true
		for _, pattern := range blacklist {
			if strings.Contains(link, pattern) {
				allowed = false
				break
			}
		}
		if allowed {
			allowedLinks = append(allowedLinks, link)
		}
	}

	return allowedLinks
}

func updateDataWeight(mData *mongo.Collection, url string, ctx context.Context) {
	filter := bson.M{"url": url}

	update := bson.M{
		"$inc": bson.M{
			"backlink": 1,
		},
	}
	opts := options.UpdateOne().SetUpsert(true)

	_, err := mData.UpdateOne(ctx, filter, update, opts)
	if err != nil {
		log.Fatal("Failed to update backlink")
	}
}

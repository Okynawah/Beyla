package com.beyla.query.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.beyla.query.repository.SiteRepository;

import jakarta.annotation.PostConstruct;
import redis.clients.jedis.Jedis;

@Service
public class StatService {

    SiteRepository siteRepository;

    Jedis jedisExplore;

    Jedis jedisVisited;

    @Value("${BEYLA_EXPLORE}")
    String jedisExploreHost;
    @Value("${BEYLA_EXPLORE_PORT}")
    String jedisExplorePort;
    @Value("${BEYLA_VISITED}")
    String jedisVisitedHost;
    @Value("${BEYLA_VISITED_PORT}")
    String jedisVisitedPort;


    @Autowired
    public StatService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @PostConstruct
    public void init() {
        jedisExplore = new Jedis(jedisExploreHost, Integer.parseInt(jedisExplorePort));
        jedisVisited = new Jedis(jedisVisitedHost, Integer.parseInt(jedisVisitedPort));
    }

    public long explore() {
        return jedisExplore.scard("queue");
    }

    public long visited() {
        return jedisVisited.scard("queue");
    }

    public long raw() {
        return siteRepository.countDocuments();
    }
}

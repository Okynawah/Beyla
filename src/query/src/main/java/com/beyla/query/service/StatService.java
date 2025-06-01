package com.beyla.query.service;

import com.beyla.query.repository.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class StatService {

    SiteRepository siteRepository;

    @Autowired
    public StatService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    Jedis jedisExplore = new Jedis("localhost", 26305);
    Jedis jedisVisited = new Jedis("localhost", 26303);


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

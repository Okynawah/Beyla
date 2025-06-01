package com.beyla.query.controller;

import com.beyla.query.model.ServerStatsRest;
import com.beyla.query.service.StatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final StatService statService;

    @Autowired
    public StatsController(StatService statService) {
        this.statService = statService;
    }

    @GetMapping("/")
    ServerStatsRest getStats() {
        long explore = this.statService.explore();
        long visited = this.statService.visited();
        long raw = this.statService.raw();
        return new ServerStatsRest(explore, raw, visited);
    }
}

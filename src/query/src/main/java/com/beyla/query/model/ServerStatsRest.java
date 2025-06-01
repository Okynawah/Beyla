package com.beyla.query.model;

public class ServerStatsRest {
    private final long rawEntries;
    private final long exploreEntries;
    private final long visitedEntries;

    public ServerStatsRest(long exploreEntries, long raw, long visited) {
        this.exploreEntries = exploreEntries;
        this.rawEntries = raw;
        this.visitedEntries = visited;
    }

    public long getRawEntries() {
        return rawEntries;
    }

    public long getExploreEntries() {
        return exploreEntries;
    }

    public long getVisitedEntries() {
        return visitedEntries;
    }
}

package com.beyla.query.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

public class SiteRest {


    private String url;

    private String title;

    private String description;

    private LocalDateTime indexed_at;

    private int score;

    public SiteRest() {
    }

    public SiteRest(String url, String title, String description, LocalDateTime indexed_at, int score) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.indexed_at = indexed_at;
        this.score = score;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getIndexed_at() {
        return indexed_at;
    }

    public int getScore() {
        return score;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIndexed_at(LocalDateTime indexed_at) {
        this.indexed_at = indexed_at;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public static SiteRest from(SiteDocument siteDocument) {
        return new SiteRest(siteDocument.getUrl(),
                siteDocument.getTitle(),
                siteDocument.getDescription(),
                siteDocument.getIndexed_at(),
                siteDocument.getScore()
        );
    }
}

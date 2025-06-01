package com.beyla.query.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.beyla.query.service.QueryService.BACKLINK_WEIGHT_COEF;
import static com.beyla.query.service.QueryService.MATCHES_WEIGHT_COEF;

@Document(collection = "windex")
public class SiteDocument implements CustomComparable<SiteDocument> {


    @Id
    @Field(name = "_id")
    private String id;

    @Field(name = "url")
    private String url;

    @Field(name = "title")
    private String title;

    @Field(name = "description")
    private String description;

    @Field(name = "indexed_at")
    private LocalDateTime indexed_at;

    @Field(name = "backlink")
    private Integer backlink;

    @Transient
    private Integer score = null;

    private Map<String, Integer> keywords;

    public SiteDocument() {
    }

    public SiteDocument(String url, String title, String description, LocalDateTime indexed_at) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.indexed_at = indexed_at;
        this.backlink = 0;
    }

    // Getters et Setters
    public String getId() {
        return id;
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

    public Map<String, Integer> getKeywords() {
        return keywords;
    }

    public Integer getBacklink() {
        return backlink;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setKeywords(Map<String, Integer> keywords) {
        this.keywords = keywords;
    }

    public void setBacklink(Integer backlink) {
        this.backlink = backlink;
    }

    @Override
    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public int compareTo(SiteDocument object, List<String> context) {
        ScoreAttribution currentScore = getScoreAttribution(this, context);

        ScoreAttribution objectScore = getScoreAttribution(object, context);

        if (objectScore.matches == currentScore.matches)
            return (int) (objectScore.weight - currentScore.weight);
        else if (objectScore.matches > currentScore.matches) {
            return 1;
        } else { // matches < currentMatches
            return -1;
        }
    }

    private static ScoreAttribution getScoreAttribution(SiteDocument object, List<String> context) {
        int objectMatches = 0;
        double objectWeight = 0;

        if (object.score == null) {
            for (String keyword : context) {
                if (object.getKeywords().containsKey(keyword)) {
                    objectWeight += object.getKeywords().get(keyword);
                    objectMatches += 1;
                }
            }
            objectWeight *= (object.backlink + 1) * BACKLINK_WEIGHT_COEF;
            objectWeight *= objectMatches * MATCHES_WEIGHT_COEF;
            object.setScore((int) objectWeight);
        } else {
            objectWeight = object.score;
        }
        ScoreAttribution objectScore = new ScoreAttribution(objectMatches, objectWeight);
        return objectScore;
    }

    private record ScoreAttribution(int matches, double weight) {
    }

    @Override
    public Integer getScore() {
        return this.score;
    }

    @Override
    public String toString() {
        return "SiteDocument{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", indexed_at=" + indexed_at +
                ", backlink=" + backlink +
                ", score=" + score +
                ", keywords=" + keywords +
                '}';
    }
}

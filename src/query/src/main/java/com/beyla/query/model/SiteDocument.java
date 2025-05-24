package com.beyla.query.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.beyla.query.service.QueryService.BACKLINK_WEIGHT_COEF;

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
        int objectMatches = 0;
        int objectWeight = 0;
        int currentMatches = 0;
        int currentWeight = 0;

        if (this.score == null) {
            for (String keyword : context) {
                if (this.keywords.containsKey(keyword)) {
                    currentWeight += this.keywords.get(keyword);
                    currentMatches += 1;
                }
            }
            currentWeight *= this.backlink * BACKLINK_WEIGHT_COEF;
            this.setScore(currentWeight);

        } else {
            currentWeight = this.score;
        }

        if (object.score == null) {
            for (String keyword : context) {
                if (object.getKeywords().containsKey(keyword)) {
                    objectWeight += object.getKeywords().get(keyword);
                    objectMatches += 1;
                }
            }
            currentWeight *= object.backlink * BACKLINK_WEIGHT_COEF;
            object.setScore(objectWeight);

        } else {
            objectWeight = object.score;
        }

        if (objectMatches == currentMatches)
            return objectWeight - currentWeight;
        else if (objectMatches > currentMatches) {
            return 1;
        } else { // objectMatches < currentMatches
            return -1;
        }
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

package com.beyla.query.service;

import com.beyla.query.model.CustomSorter;
import com.beyla.query.model.SiteDocument;
import com.beyla.query.repository.SiteRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService {
    public static final double BEST_RESULT_SCORE_THRESHOLD = .07;
    public static final double BACKLINK_WEIGHT_COEF = 0.75;
    public static final double MATCHES_WEIGHT_COEF = 0.25;    

    SiteRepository siteRepository;

    public QueryService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public List<SiteDocument> search(List<String> queryKeywords, @Positive int page, @Min(5) @Max(100) int size) {
        List<SiteDocument> sites = siteRepository.findByAnyKeyword(queryKeywords);
        return sites;
    }

    public List<SiteDocument> sort(List<SiteDocument> query, List<String> queryKeywords) {
        return CustomSorter.sort(query, queryKeywords);
    }

    public List<SiteDocument> crossQuery(List<SiteDocument> query, List<String> queryKeywords) {
        return query;
    }

    // only allow score more than BEST_RESULT_SCORE_THRESHOLD% of the best score, without taking into account number of keyword matches
    public List<SiteDocument> bestResults(List<SiteDocument> query) {
        int bestResultScore = query.stream().reduce(0, (result, object) -> Math.max(result, object.getScore()), Integer::max);
        return query.parallelStream().filter(siteDocument -> siteDocument.getScore() > Math.max(bestResultScore * BEST_RESULT_SCORE_THRESHOLD, 1)).toList();
    }
}

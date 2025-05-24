package com.beyla.query.service;

import com.beyla.query.model.CustomSorter;
import com.beyla.query.model.SiteDocument;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService {
    public static final double BEST_RESULT_SCORE_THRESHOLD = .07;
    public static final double BACKLINK_WEIGHT_COEF = 0.75;

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

    // only allow score more than BEST_RESULT_SCORE_THRESHOLD% of the best score
    public List<SiteDocument> bestResults(List<SiteDocument> sort) {
        int bestResultScore = sort.get(0).getScore();
        return sort.parallelStream().filter(siteDocument -> siteDocument.getScore() > Math.max(bestResultScore * BEST_RESULT_SCORE_THRESHOLD, 1)).toList();
    }
}

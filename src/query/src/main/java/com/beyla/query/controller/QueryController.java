package com.beyla.query.controller;

import com.beyla.query.model.SiteDocument;
import com.beyla.query.model.SiteRest;
import com.beyla.query.service.QueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class QueryController {
    QueryService queryService;

    @Autowired
    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/query")
    public Page<SiteRest> query(@RequestParam(name = "q") String query,
                                    @RequestParam(name = "p", defaultValue = "1") @Positive int page,
                                    @RequestParam(name = "s", defaultValue = "10") @Min(5) @Max(1000) int size) {
        List<String> queryKeywords = List.of(query.split(" "));
        List<SiteDocument> search = queryService.search(queryKeywords, page, size);
        if (search.isEmpty())
            return new PageImpl<>(List.of(), Pageable.ofSize(1).withPage(0), 0);
        if (search.size() == 1) { // special case because score attribution is on sort
            SiteDocument singleSiteDocument = search.get(0);
            singleSiteDocument.setScore(1);
            return new PageImpl<>(List.of(SiteRest.from(singleSiteDocument)), Pageable.ofSize(1).withPage(1), 1);
        }
        List<SiteDocument> sort = queryService.sort(search, queryKeywords);
        List<SiteDocument> crossQuery = queryService.crossQuery(sort, queryKeywords);
        List<SiteDocument> bestResults = queryService.bestResults(crossQuery);
        return new PageImpl<>(bestResults.stream().map(SiteRest::from).toList(), Pageable.ofSize(size).withPage(page), size);
    }

}
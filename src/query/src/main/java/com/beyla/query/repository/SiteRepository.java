package com.beyla.query.repository;

import com.beyla.query.model.SiteDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class SiteRepository  {

    @Autowired
    MongoTemplate mongoTemplate;

    public List<SiteDocument> findByAnyKeyword(List<String> queryKeywords) {
        List<Criteria> criteriaList = new ArrayList<>();

        for (String keyword : queryKeywords) {
            criteriaList.add(Criteria.where("keywords." + keyword).exists(true));
        }

        Query query = new Query(new Criteria().orOperator(criteriaList.toArray(new Criteria[0])));
        return mongoTemplate.find(query, SiteDocument.class);
    }

    public long countDocuments() {
        return mongoTemplate.estimatedCount("windex");
    }
}

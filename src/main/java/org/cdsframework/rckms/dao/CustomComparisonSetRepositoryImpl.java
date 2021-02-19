package org.cdsframework.rckms.dao;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import org.cdsframework.rckms.dao.ComparisonSet.Status;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.client.result.UpdateResult;

public class CustomComparisonSetRepositoryImpl implements CustomComparisonSetRepository
{
  private MongoTemplate mongoTemplate;

  public CustomComparisonSetRepositoryImpl(MongoTemplate mongoTemplate)
  {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public int addOrUpdate(ComparisonTest test, String comparisonSetKey)
  {
    Query query = new Query();
    query.addCriteria(Criteria.where("comparisonSetKey").is(comparisonSetKey));
    Update update = new Update()
        .inc("serviceOutputCount", 1)
        .setOnInsert("status", Status.INCOMPLETE)
        .setOnInsert("comparisonTestId", test.getId())
        .setOnInsert("comparisonSetKey", comparisonSetKey)
        .setOnInsert("createDate", OffsetDateTime.now());

    UpdateResult result = mongoTemplate.upsert(query, update, ComparisonSet.class);
    return (int) result.getMatchedCount();
  }

  @Override
  public int markReadyForComparison(String comparisonSetKey)
  {
    Query query = new Query();
    query.addCriteria(Criteria.where("comparisonSetKey").is(comparisonSetKey));
    Update update = new Update()
        .set("status", Status.PENDING)
        .set("comparisonDate", null)
        .set("results", new ArrayList<>());

    UpdateResult result = mongoTemplate.updateFirst(query, update, ComparisonSet.class);
    return (int) result.getMatchedCount();
  }
}

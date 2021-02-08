package org.cdsframework.rckms.dao;

import java.time.OffsetDateTime;

import org.cdsframework.rckms.dao.QueueRecord.QueueStatus;
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
  public int addOrUpdate(String comparisonSetKey)
  {
    Query query = new Query();
    query.addCriteria(Criteria.where("comparisonSetKey").is(comparisonSetKey));
    Update update = new Update()
        .inc("serviceOutputCount", 1)
        .setOnInsert("comparisonSetKey", comparisonSetKey)
        .setOnInsert("createDate", OffsetDateTime.now());

    UpdateResult result = mongoTemplate.upsert(query, update, ComparisonSet.class);
    return (int)result.getMatchedCount();
  }
}

package org.cdsframework.rckms.dao;

import org.springframework.data.mongodb.core.MongoTemplate;

public class CustomQueueRepositoryImpl implements CustomQueueRepository
{
  private MongoTemplate mongoTemplate;

  public CustomQueueRepositoryImpl(MongoTemplate mongoTemplate)
  {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public void addQueueRecord(QueueRecord queueRecord)
  {
    //    Query query = new Query();
    //    query.addCriteria(Criteria.where("comparisonSetKey").is(queueRecord.getComparisonSetKey()));
    //    Update update = new Update()
    //        .set("statusDate", OffsetDateTime.now())
    //        .setOnInsert("comparisonSetKey", queueRecord.getComparisonSetKey())
    //        .setOnInsert("createDate", OffsetDateTime.now())
    //        .setOnInsert("status", QueueStatus.PENDING)
    //        .setOnInsert("statusDate", OffsetDateTime.now());
    //
    //    UpdateResult result = mongoTemplate.upsert(query, update, QueueRecord.class);
  }
}

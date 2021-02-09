package org.cdsframework.rckms.dao;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.cdsframework.rckms.dao.QueueRecord.QueueStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class CustomQueueRepositoryImpl implements CustomQueueRepository
{
  private MongoTemplate mongoTemplate;

  public CustomQueueRepositoryImpl(MongoTemplate mongoTemplate)
  {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public Optional<QueueRecord> takeNextPending()
  {
    Query query = new Query();
    query.addCriteria(Criteria.where("status").is(QueueStatus.PENDING))
        .with(Sort.by("createDate"));
    Update update = new Update();
    update.set("status", QueueStatus.PROCESSING)
        .set("statusDate", OffsetDateTime.now());
    FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
    return Optional.ofNullable(mongoTemplate.findAndModify(query, update, options, QueueRecord.class));
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

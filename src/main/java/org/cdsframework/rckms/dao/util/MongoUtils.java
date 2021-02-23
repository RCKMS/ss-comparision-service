package org.cdsframework.rckms.dao.util;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.query.Criteria;

public class MongoUtils
{
  public static Criteria andBetween(Criteria criteria, String fieldName, OffsetDateTime start, OffsetDateTime end)
  {
    if (start != null && end != null)
      criteria.and(fieldName).gte(start).lte(end);
    else if (start != null)
      criteria.and(fieldName).gte(start);
    else if (end != null)
      criteria.and(fieldName).lte(end);
    return criteria;
  }

  public static Optional<IndexInfo> findFieldIndex(MongoTemplate mongoTemplate, Class entityType, String fieldName)
  {
    List<IndexInfo> indexes = mongoTemplate.indexOps(entityType).getIndexInfo();
    return indexes.stream()
        .filter(index -> index.getIndexFields().size() == 1)
        .filter(index -> index.getIndexFields().get(0).getKey().equals(fieldName))
        .findAny();
  }

  public static void dropIndex(MongoTemplate mongoTemplate, Class entityType, String indexName)
  {
    mongoTemplate.indexOps(entityType).dropIndex(indexName);
  }

  public static void addIndex(MongoTemplate mongoTemplate, Class entityType, Index index)
  {
    mongoTemplate.indexOps(entityType).ensureIndex(index);
  }
}

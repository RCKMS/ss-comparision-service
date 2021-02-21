package org.cdsframework.rckms.dao.util;

import java.time.OffsetDateTime;

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
}

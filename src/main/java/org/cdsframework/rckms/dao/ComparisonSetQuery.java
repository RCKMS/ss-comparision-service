package org.cdsframework.rckms.dao;

import java.time.OffsetDateTime;

import org.cdsframework.rckms.dao.ComparisonSet.Status;
import org.cdsframework.rckms.dao.util.MongoUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class ComparisonSetQuery
{
  private String comparisonTestId;
  private OffsetDateTime start;
  private OffsetDateTime end;
  private Status status;
  private Pageable pageable;

  public ComparisonSetQuery(String testId, Pageable pageable)
  {
    this.comparisonTestId = testId;
    this.pageable = pageable;
  }

  public ComparisonSetQuery onOrAfter(OffsetDateTime date)
  {
    this.start = date;
    return this;
  }

  public ComparisonSetQuery onOrBefore(OffsetDateTime date)
  {
    this.end = date;
    return this;
  }

  public ComparisonSetQuery withStatus(Status status)
  {
    this.status = status;
    return this;
  }

  public Criteria toCriteria()
  {
    Criteria criteria = Criteria.where("comparisonTestId").is(comparisonTestId);
    MongoUtils.andBetween(criteria, "createDate", start, end);
    if (status != null)
      criteria.and("status").is(status);
    return criteria;
  }

  public Query toQuery()
  {
    Query query = new Query();
    query.addCriteria(toCriteria());
    if (pageable != null)
      query.with(pageable);
    return query;
  }

  public String getComparisonTestId()
  {
    return comparisonTestId;
  }

  public OffsetDateTime getStart()
  {
    return start;
  }

  public OffsetDateTime getEnd()
  {
    return end;
  }

  public Status getStatus()
  {
    return status;
  }

  public Pageable getPageable()
  {
    return pageable;
  }
}

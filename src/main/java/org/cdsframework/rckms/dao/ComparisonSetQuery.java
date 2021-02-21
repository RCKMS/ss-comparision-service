package org.cdsframework.rckms.dao;

import java.time.OffsetDateTime;

import org.cdsframework.rckms.dao.ComparisonSet.Status;
import org.cdsframework.rckms.dao.util.MongoUtils;
import org.springframework.data.mongodb.core.query.Criteria;

public class ComparisonSetQuery
{
  private String comparisonTestId;
  private OffsetDateTime start;
  private OffsetDateTime end;
  private Status status;

  public ComparisonSetQuery(String testId)
  {
    this.comparisonTestId = testId;
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
}

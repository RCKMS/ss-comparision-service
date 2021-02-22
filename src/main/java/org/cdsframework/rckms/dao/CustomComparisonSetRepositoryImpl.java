package org.cdsframework.rckms.dao;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cdsframework.rckms.dao.ComparisonResult.Type;
import org.cdsframework.rckms.dao.ComparisonSet.Status;
import org.cdsframework.rckms.dao.util.MongoUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;

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

  @Override
  public Page<ComparisonSet> findComparisonSets(ComparisonSetQuery queryDef)
  {
    Query query = queryDef.toQuery();
    List<ComparisonSet> results = mongoTemplate.find(query, ComparisonSet.class);
    return PageableExecutionUtils.getPage(results, queryDef.getPageable(),
        // This gets the total count. limit(-1).skip(-1) overrides the limit/skip set by the query above
        () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ComparisonSet.class));
  }

  @Override
  public Map<Status, Integer> statusCounts(String comparisonTestId, OffsetDateTime start, OffsetDateTime end)
  {
    Criteria match = Criteria.where("comparisonTestId").is(comparisonTestId);
    MongoUtils.andBetween(match, "createDate", start, end);

    Aggregation agg = newAggregation(
        match(match),
        group("status").count().as("count")
    );
    AggregationResults<GroupCount> groupResults
        = mongoTemplate.aggregate(agg, ComparisonSet.class, GroupCount.class);
    return groupResults.getMappedResults().stream()
        .collect(Collectors.toMap(group -> Status.valueOf(group.getGroup()), GroupCount::getCount));
  }

  @Override
  public Map<Type, Integer> failureTypeCounts(String comparisonTestId, OffsetDateTime start, OffsetDateTime end)
  {
    Criteria match = Criteria.where("comparisonTestId").is(comparisonTestId);
    MongoUtils.andBetween(match, "createDate", start, end);

    Aggregation agg = newAggregation(
        match(match),
        unwind("results"),
        group("results.type").count().as("count")
    );
    AggregationResults<GroupCount> groupResults
        = mongoTemplate.aggregate(agg, ComparisonSet.class, GroupCount.class);
    return groupResults.getMappedResults().stream()
        .filter(group -> group.getGroup() != null)
        .collect(Collectors.toMap(group -> Type.valueOf(group.getGroup()), GroupCount::getCount));
  }

  public static class GroupCount
  {
    @Id
    private String group;
    private int count;

    public String getGroup()
    {
      return group;
    }

    public void setGroup(String group)
    {
      this.group = group;
    }

    public int getCount()
    {
      return count;
    }

    public void setCount(int count)
    {
      this.count = count;
    }
  }

}

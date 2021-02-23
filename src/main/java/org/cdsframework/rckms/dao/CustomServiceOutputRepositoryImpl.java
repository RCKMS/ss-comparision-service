package org.cdsframework.rckms.dao;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cdsframework.rckms.dao.util.MongoUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class CustomServiceOutputRepositoryImpl implements CustomServiceOutputRepository
{
  private MongoTemplate mongoTemplate;

  public CustomServiceOutputRepositoryImpl(MongoTemplate mongoTemplate)
  {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public Map<String, Integer> countsForTestGroupedBySource(String testId, OffsetDateTime start, OffsetDateTime end)
  {
    Criteria match = Criteria.where("comparisonTestId").is(testId);
    MongoUtils.andBetween(match, "createDate", start, end);

    Aggregation agg = newAggregation(
        match(match),
        group("sourceId").count().as("count")
    );
    AggregationResults<SourceCount> groupResults
        = mongoTemplate.aggregate(agg, ServiceOutput.class, SourceCount.class);
    return groupResults.getMappedResults().stream().collect(Collectors.toMap(SourceCount::getSourceId, SourceCount::getCount));
  }

  @Override
  public void markCompared(List<ServiceOutput> outputs, OffsetDateTime comparisonDate)
  {
    List<String> ids = outputs.stream().map(ServiceOutput::getId).collect(Collectors.toList());
    Query query = new Query();
    query.addCriteria(Criteria.where("id").in(ids));
    Update update = new Update();
    update.set("comparisonDate", comparisonDate);
    mongoTemplate.updateMulti(query, update, ServiceOutput.class);
  }

  public static class SourceCount
  {
    @Id
    private String sourceId;
    private int count;

    public String getSourceId()
    {
      return sourceId;
    }

    public void setSourceId(String sourceId)
    {
      this.sourceId = sourceId;
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

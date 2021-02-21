package org.cdsframework.rckms.rest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.cdsframework.rckms.dao.ComparisonResult.Type;
import org.cdsframework.rckms.dao.ComparisonSet.Status;

public class ComparisonTestSummary
{
  private String comparisonTestId;
  private OffsetDateTime startDate;
  private OffsetDateTime endDate;
  private ComparisonSetInfo comparisonSets;
  private ServiceOutputInfo payloads;

  public ComparisonTestSummary(String testId)
  {
    this.comparisonTestId = testId;
  }

  public void addComparisonSetInfo(Map<Status, Integer> statusCounts, Map<Type, Integer> failureTypeCounts)
  {
    comparisonSets = new ComparisonSetInfo(statusCounts, failureTypeCounts);
  }

  public ComparisonSetInfo getComparisonSets()
  {
    return comparisonSets;
  }

  public void addServiceOutputCounts(Map<String, Integer> countsBySource)
  {
    payloads = new ServiceOutputInfo(countsBySource);
  }

  public ServiceOutputInfo getPayloads()
  {
    return payloads;
  }

  public String getComparisonTestId()
  {
    return comparisonTestId;
  }

  public OffsetDateTime getStartDate()
  {
    return startDate;
  }

  public void setStartDate(OffsetDateTime startDate)
  {
    this.startDate = startDate;
  }

  public OffsetDateTime getEndDate()
  {
    return endDate;
  }

  public void setEndDate(OffsetDateTime endDate)
  {
    this.endDate = endDate;
  }

  public static final class ComparisonSetInfo
  {
    private final int totalComparisonSets;
    private final Map<Status, AggregateStats> statusDistribution = new HashMap<>();

    private int totalFailures;
    private final Map<Type, AggregateStats> failureTypeDistribution = new HashMap<>();

    public ComparisonSetInfo(Map<Status, Integer> statusDistribution, Map<Type, Integer> failureTypeDistribution)
    {
      totalComparisonSets = statusDistribution.values().stream().collect(Collectors.summingInt(Integer::intValue));
      statusDistribution.forEach((status, count) -> this.statusDistribution.put(status, new AggregateStats(count,
          totalComparisonSets)));

      totalFailures = failureTypeDistribution.values().stream().collect(Collectors.summingInt(Integer::intValue));
      failureTypeDistribution.forEach((type, count) -> this.failureTypeDistribution.put(type, new AggregateStats(count,
          totalFailures)));
    }

    public int getTotalComparisonSets()
    {
      return totalComparisonSets;
    }

    public Map<Status, AggregateStats> getStatusDistribution()
    {
      return statusDistribution;
    }

    public int getTotalFailures()
    {
      return totalFailures;
    }

    public Map<Type, AggregateStats> getFailureTypeDistribution()
    {
      return failureTypeDistribution;
    }
  }

  public static final class AggregateStats
  {
    private final int count;
    private BigDecimal ratio;

    AggregateStats(int count, int total)
    {
      this.count = count;
      if (total == 0)
        this.ratio = BigDecimal.ZERO;
      else
        this.ratio = new BigDecimal((double) count / (double) total).setScale(4, RoundingMode.HALF_UP);
    }

    public int getCount()
    {
      return count;
    }

    public BigDecimal getRatio()
    {
      return ratio;
    }
  }

  public static final class ServiceOutputInfo
  {
    private final int totalCount;
    private final Map<String, AggregateStats> sourceDistribution = new HashMap<>();

    ServiceOutputInfo(Map<String, Integer> countsBySource)
    {
      totalCount = countsBySource.values().stream().collect(Collectors.summingInt(Integer::intValue));
      countsBySource.forEach((source, count) -> this.sourceDistribution.put(source, new AggregateStats(count, totalCount)));
    }

    public int getTotalCount()
    {
      return totalCount;
    }

    public Map<String, AggregateStats> getSourceDistribution()
    {
      return sourceDistribution;
    }
  }
}

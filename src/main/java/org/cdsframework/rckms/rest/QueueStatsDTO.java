package org.cdsframework.rckms.rest;

import org.cdsframework.rckms.QueueProcessingService.ProcessingStatsCollector;

public class QueueStatsDTO
{
  private ProcessingStatsCollector.Stats processingStats;
  private long pendingCount;

  public QueueStatsDTO()
  {
  }

  public void setProcessingStats(ProcessingStatsCollector.Stats stats)
  {
    this.processingStats = stats;
  }

  public ProcessingStatsCollector.Stats getProcessingStats()
  {
    return processingStats;
  }

  public long getPendingCount()
  {
    return pendingCount;
  }

  public void setPendingCount(long pendingCount)
  {
    this.pendingCount = pendingCount;
  }

  //  public static final class QueueProcessingStats
  //  {
  //    private String description = "Stats since server startup";
  //    private OffsetDateTime lastProcessDate;
  //    private long errorCount;
  //    private long matchesCount;
  //    private long differencesCount;
  //    private long totalCount;
  //    private double averageProcessingTimePerRecord;
  //    private double maxProcessingTimePerRecord;
  //
  //    public QueueProcessingStats(ProcessingStatsCollector.Stats stats)
  //    {
  //      setErrorCount(stats.getErrors());
  //      setMatchesCount(stats.getMatches());
  //      setDifferencesCount(stats.getDifferences());
  //      setLastProcessDate(stats.getLastProcessDate());
  //      totalCount = getErrorCount() + getDifferencesCount() + getMatchesCount();
  //      averageProcessingTimePerRecord = stats.getAverageProcessingTime();
  //      maxProcessingTimePerRecord = stats.getMaxProcessingTime();
  //    }
  //
  //    public long getErrorCount()
  //    {
  //      return errorCount;
  //    }
  //
  //    public void setErrorCount(long errorCount)
  //    {
  //      this.errorCount = errorCount;
  //    }
  //
  //    public long getMatchesCount()
  //    {
  //      return matchesCount;
  //    }
  //
  //    public void setMatchesCount(long matchesCount)
  //    {
  //      this.matchesCount = matchesCount;
  //    }
  //
  //    public long getDifferencesCount()
  //    {
  //      return differencesCount;
  //    }
  //
  //    public void setDifferencesCount(long differencesCount)
  //    {
  //      this.differencesCount = differencesCount;
  //    }
  //
  //    public long getTotalCount()
  //    {
  //      return totalCount;
  //    }
  //
  //    public BigDecimal getErrorRatio()
  //    {
  //      if (getTotalCount() == 0)
  //        return BigDecimal.ZERO;
  //      return new BigDecimal(getErrorCount()).divide(new BigDecimal(getTotalCount()), 4, RoundingMode.HALF_UP);
  //    }
  //
  //    public BigDecimal getMatchesRatio()
  //    {
  //      if (getTotalCount() == 0)
  //        return BigDecimal.ZERO;
  //      return new BigDecimal(getMatchesCount()).divide(new BigDecimal(getTotalCount()), 4, RoundingMode.HALF_UP);
  //    }
  //
  //    public OffsetDateTime getLastProcessDate()
  //    {
  //      return lastProcessDate;
  //    }
  //
  //    public void setLastProcessDate(OffsetDateTime lastProcessDate)
  //    {
  //      this.lastProcessDate = lastProcessDate;
  //    }
  //
  //    public String getDescription()
  //    {
  //      return description;
  //    }
  //
  //    public double getAverageProcessingTimePerRecord()
  //    {
  //      return averageProcessingTimePerRecord;
  //    }
  //
  //    public double getMaxProcessingTimePerRecord()
  //    {
  //      return maxProcessingTimePerRecord;
  //    }
  //  }
}

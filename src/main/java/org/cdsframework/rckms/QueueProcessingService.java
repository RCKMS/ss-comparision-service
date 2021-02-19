package org.cdsframework.rckms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.cdsframework.rckms.compare.ComparisonService;
import org.cdsframework.rckms.dao.ComparisonResult;
import org.cdsframework.rckms.dao.QueueRecord;
import org.cdsframework.rckms.dao.QueueRecord.QueueStatus;
import org.cdsframework.rckms.dao.QueueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;

//@ConditionalOnProperty(prefix = "processor", name = "enabled", havingValue = "true")
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class QueueProcessingService
{
  private static final Logger logger = LoggerFactory.getLogger(QueueProcessingService.class);
  private static final String QUEUE_PROCESS_TIMER = "queue.process";
  private static final String QUEUE_POLL_TIMER = "queue.poll";

  @Autowired
  private QueueProcessingService self;

  private ProcessorConfig config;
  private QueueRepository queueRepo;
  private ComparisonService comparisonService;
  private ProcessingStatsCollector processingStatsCollector;

  public QueueProcessingService(ProcessorConfig processorConfig, QueueRepository queueRepo, ComparisonService comparisonService,
      ProcessingStatsCollector stats)
  {
    this.config = processorConfig;
    this.queueRepo = queueRepo;
    this.comparisonService = comparisonService;
    this.processingStatsCollector = stats;
  }

  @Scheduled(fixedDelayString = "#{@processorConfig.queryFrequency.toMillis()}")
  public void process()
  {
    if (!config.isEnabled())
      return;

    StopWatch sw = new StopWatch();
    sw.start();
    logger.info("Querying for PENDING queue records...");
    int batchCount = 0;
    QueueRecord nextRecord;
    while ((nextRecord = self._getNext()) != null)
    {
      batchCount++;
      self.process(nextRecord);
    }
    sw.stop();
    if (batchCount > 0)
      logger.info("Processed {} queue record(s) in {}ms", batchCount, sw.getTotalTimeMillis());

    logger.info("Queue is empty, sleeping for {}s...", config.getQueryFrequency().getSeconds());
  }

  // Only public to allow proxying. This should NOT be called outside of this class
  @Timed(value = QUEUE_POLL_TIMER, description = "Queue polling operation timer", percentiles = { 0.5, 0.95 })
  public QueueRecord _getNext()
  {
    return queueRepo.takeNextPending().orElse(null);
  }

  @Async("queueProcessorExecutor")
  @Timed(value = QUEUE_PROCESS_TIMER, description = "Queue record processing timer", percentiles = { 0.5, 0.95 })
  public void process(QueueRecord record)
  {
    logger.trace("Processing queue record: id={}; comparisonSetKey={}", record.getId(), record.getComparisonSetKey());
    try
    {
      List<ComparisonResult> results = compare(record.getComparisonSetKey());
      onComplete(record);
      if (results.isEmpty())
        processingStatsCollector.addMatch();
      else
        processingStatsCollector.addDifference();
      logger.trace("Processed queue record successfully: id={}; comparisonSetKey={}; diffCount: {}", record.getId(),
          record.getComparisonSetKey(), results.size());
    }
    catch (Exception e)
    {
      logger.error("Error processing queue record " + record + ": " + e.getMessage(), e);
      onError(record, e.getMessage());
      processingStatsCollector.addError();
    }

  }

  private List<ComparisonResult> compare(String comparisonSetKey)
  {
    List<ComparisonResult> results = comparisonService.runComparison(comparisonSetKey);
    return results;
  }

  private void onComplete(QueueRecord record)
  {
    if (config.isDeleteOnComplete())
      // Physically deleting the queue record for successful records ensures we keep the queue
      // collection small and therefore quick to query
      deleteQueueRecord(record);
    else
      updateQueueRecord(record, QueueStatus.COMPLETE, null);
  }

  private void onError(QueueRecord record, String error)
  {
    updateQueueRecord(record, QueueStatus.ERROR, error);
  }

  private void updateQueueRecord(QueueRecord record, QueueStatus status, String error)
  {
    record.setStatus(status);
    record.setStatusDate(OffsetDateTime.now());
    record.setError(error);
    queueRepo.save(record);
  }

  private void deleteQueueRecord(QueueRecord record)
  {
    try
    {
      queueRepo.delete(record);
    }
    catch (Exception e)
    {
      logger.error("Error deleting queue record " + record + ": " + e.getMessage(), e);
    }
  }

  public ProcessingStatsCollector.Stats getProcessingStats()
  {
    return processingStatsCollector.getCurrentStats();
  }

  @Component
  public static class ProcessingStatsCollector
  {
    //private final AtomicLong errors = new AtomicLong();
    //private final AtomicLong differences = new AtomicLong();
    //private final AtomicLong matches = new AtomicLong();
    private OffsetDateTime lastProcessDate;

    private MeterRegistry meterRegistry;
    // private Counter totalCounter;
    private Counter errorCounter;
    private Counter matchesCounter;
    private Counter differencesCounter;

    public ProcessingStatsCollector(MeterRegistry meterRegistry)
    {
      this.meterRegistry = meterRegistry;
      //      totalCounter = Counter.builder("queue.count")
      //          .description("Total count of queue records processed since startup")
      //          .register(meterRegistry);
      errorCounter = Counter.builder("queue.count")
          .description("Total count of queue records processed since startup")
          .tag("type", "error")
          .register(meterRegistry);
      matchesCounter = Counter.builder("queue.count")
          .description("Total count of queue records processed since startup")
          .tag("type", "matches")
          .register(meterRegistry);
      differencesCounter = Counter.builder("queue.count")
          .description("Total count of queue records processed since startup")
          .tag("type", "differences")
          .register(meterRegistry);
    }

    void addMatch()
    {
      //matches.incrementAndGet();
      lastProcessDate = OffsetDateTime.now();
      matchesCounter.increment();
    }

    void addError()
    {
      //errors.incrementAndGet();
      lastProcessDate = OffsetDateTime.now();
      errorCounter.increment();
    }

    void addDifference()
    {
      //differences.incrementAndGet();
      lastProcessDate = OffsetDateTime.now();
      differencesCounter.increment();
    }

    public Stats getCurrentStats()
    {
      Optional<HistogramSnapshot> processingStats = getTimer(QUEUE_PROCESS_TIMER).map(Timer::takeSnapshot);
      Optional<HistogramSnapshot> pollingStats = getTimer(QUEUE_POLL_TIMER).map(Timer::takeSnapshot);
      return new Stats(lastProcessDate)
          // Note that these first 3 cannot be an atomic snapshot, resulting in possibly
          // slight skewing of the figures
          .differences((long) differencesCounter.count())
          .matches((long) matchesCounter.count())
          .differences((long) differencesCounter.count())
          .processingTime(
              processingStats.map(s -> s.mean(TimeUnit.MILLISECONDS)).orElse(0d),
              processingStats.map(s -> s.max(TimeUnit.MILLISECONDS)).orElse(0d),
              processingStats.map(HistogramSnapshot::percentileValues).orElse(new ValueAtPercentile[] {}))
          .pollingTime(
              pollingStats.map(s -> s.mean(TimeUnit.MILLISECONDS)).orElse(0d),
              pollingStats.map(s -> s.max(TimeUnit.MILLISECONDS)).orElse(0d),
              pollingStats.map(HistogramSnapshot::percentileValues).orElse(new ValueAtPercentile[] {}));
    }

    //    public long getErrors()
    //    {
    //      //return errors.get();
    //      return (long) errorCounter.count();
    //    }
    //
    //    public long getDifferences()
    //    {
    //      //return differences.get();
    //      return (long) differencesCounter.count();
    //    }
    //
    //    public long getMatches()
    //    {
    //      //return matches.get();
    //      return (long) matchesCounter.count();
    //    }
    //
    //    public OffsetDateTime getLastProcessDate()
    //    {
    //      return lastProcessDate;
    //    }
    //
    //    public double getAverageProcessingTime()
    //    {
    //      return getTimer(QUEUE_PROCESS_TIMER).map(timer -> timer.mean(TimeUnit.MILLISECONDS)).orElse(0d);
    //    }
    //
    //    public double getMaxProcessingTime()
    //    {
    //      return getTimer(QUEUE_PROCESS_TIMER).map(timer -> timer.max(TimeUnit.MILLISECONDS)).orElse(0d);
    //    }

    private Optional<Timer> getTimer(String name)
    {
      return Optional.ofNullable(meterRegistry.find(name).timer());
    }

    public static class Stats
    {
      private OffsetDateTime lastProcessDate;
      private long errors;
      private long matches;
      private long differences;
      //      private long total;
      //      private BigDecimal errorRatio;
      //      private BigDecimal matchesRatio;
      //      private BigDecimal differencesRatio;
      private BigDecimal processingTimeAvgMs;
      private BigDecimal processingTimeMaxMs;
      private ValueAtPercentile[] processingTimePercentiles;
      private BigDecimal pollingTimeAvgMs;
      private BigDecimal pollingTimeMaxMs;
      private ValueAtPercentile[] pollingTimePercentiles;

      Stats(OffsetDateTime lastProcessDate)
      {
        this.lastProcessDate = lastProcessDate;
      }

      Stats processingTime(double avg, double max, ValueAtPercentile[] percentiles)
      {
        this.processingTimeAvgMs = new BigDecimal(avg).setScale(4, RoundingMode.HALF_UP);
        this.processingTimeMaxMs = new BigDecimal(max).setScale(4, RoundingMode.HALF_UP);
        this.processingTimePercentiles = percentiles;
        return this;
      }

      Stats pollingTime(double avg, double max, ValueAtPercentile[] percentiles)
      {
        this.pollingTimeAvgMs = new BigDecimal(avg).setScale(4, RoundingMode.HALF_UP);
        this.pollingTimeMaxMs = new BigDecimal(max).setScale(4, RoundingMode.HALF_UP);
        this.pollingTimePercentiles = percentiles;
        return this;
      }

      Stats errors(long errors)
      {
        this.errors = errors;
        return this;
      }

      Stats matches(long matches)
      {
        this.matches = matches;
        return this;
      }

      Stats differences(long differences)
      {
        this.differences = differences;
        return this;
      }

      public long getTotal()
      {
        return errors + matches + differences;
      }

      public OffsetDateTime getLastProcessDate()
      {
        return lastProcessDate;
      }

      public long getErrors()
      {
        return errors;
      }

      public long getMatches()
      {
        return matches;
      }

      public long getDifferences()
      {
        return differences;
      }

      public BigDecimal getErrorRatio()
      {
        if (getTotal() == 0)
          return BigDecimal.ZERO;
        return new BigDecimal(getErrors()).divide(new BigDecimal(getTotal()), 4, RoundingMode.HALF_UP);
      }

      public BigDecimal getMatchesRatio()
      {
        if (getTotal() == 0)
          return BigDecimal.ZERO;
        return new BigDecimal(getMatches()).divide(new BigDecimal(getTotal()), 4, RoundingMode.HALF_UP);
      }

      public BigDecimal getDifferencesRatio()
      {
        if (getTotal() == 0)
          return BigDecimal.ZERO;
        return new BigDecimal(getDifferences()).divide(new BigDecimal(getTotal()), 4, RoundingMode.HALF_UP);
      }

      public BigDecimal getProcessingTimeAvgMs()
      {
        return processingTimeAvgMs;
      }

      public BigDecimal getProcessingTimeMaxMs()
      {
        return processingTimeMaxMs;
      }

      public BigDecimal getPollingTimeAvgMs()
      {
        return pollingTimeAvgMs;
      }

      public BigDecimal getPollingTimeMaxMs()
      {
        return pollingTimeMaxMs;
      }

      public Map<Double, BigDecimal> getProcessingTimePercentiles()
      {
        return Arrays.stream(processingTimePercentiles)
            .collect(Collectors.toMap(
                ValueAtPercentile::percentile,
                p -> new BigDecimal(p.value(TimeUnit.MILLISECONDS)).setScale(4, RoundingMode.HALF_UP),
                (x, y) -> y,
                LinkedHashMap::new));
      }

      public Map<Double, BigDecimal> getPollingTimePercentiles()
      {
        return Arrays.stream(pollingTimePercentiles)
            .collect(Collectors.toMap(
                ValueAtPercentile::percentile,
                p -> new BigDecimal(p.value(TimeUnit.MILLISECONDS)).setScale(4, RoundingMode.HALF_UP),
                (x, y) -> y,
                LinkedHashMap::new));
      }
    }
  }
}

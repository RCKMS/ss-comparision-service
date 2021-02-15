package org.cdsframework.rckms;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.cdsframework.rckms.dao.ComparisonResult;
import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ComparisonSet.Status;
import org.cdsframework.rckms.dao.QueueRecord;
import org.cdsframework.rckms.dao.QueueRecord.QueueStatus;
import org.cdsframework.rckms.dao.QueueRepository;
import org.cdsframework.rckms.dao.ServiceOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(prefix = "processor", name = "enabled", havingValue = "true")
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ProcessingService
{
  private static final Logger logger = LoggerFactory.getLogger(ProcessingService.class);

  @Autowired
  private ProcessingService self;

  private ProcessorConfig config;
  private QueueRepository queueRepo;
  private ManagementService managementService;

  public ProcessingService(ProcessorConfig processorConfig, QueueRepository queueRepo, ManagementService managementService)
  {
    this.config = processorConfig;
    this.queueRepo = queueRepo;
    this.managementService = managementService;
  }

  @Scheduled(fixedDelayString = "#{@processorConfig.queryFrequency.toMillis()}")
  public void process()
  {
    logger.info("Querying for PENDING queue records...");
    QueueRecord nextRecord;
    while ((nextRecord = getNext()) != null)
    {
      self.process(nextRecord);
    }

    logger.info("Queue is empty, sleeping for {}s...", config.getQueryFrequency().getSeconds());
  }

  private QueueRecord getNext()
  {
    return queueRepo.takeNextPending().orElse(null);
  }

  @Async("queueProcessorExecutor")
  public void process(QueueRecord record)
  {
    logger.info("Processing queue record: id={}; comparisonSetKey={}", record.getId(), record.getComparisonSetKey());
    try
    {
      ComparisonSet comparisonSet = getComparisonSet(record);
      List<ServiceOutput> outputs = getServiceOutputs(record);

      // 1. Do the comparison
      List<ComparisonResult> results = compare(comparisonSet, outputs);

      // 2. Save the ComparisonSet record
      saveComparisonResults(comparisonSet, results);

      // 3. Remove the QueueRecord. Physically deleting the queue record for successful records ensures we keep the queue
      // collection small and therefore quick to query
      onComplete(record);

      logger.info("Processed queue record successfully: id={}; comparisonSetKey={}", record.getId(), record.getComparisonSetKey());
    }
    catch (Exception e)
    {
      logger.error("Error processing queue record " + record + ": " + e.getMessage(), e);
      onError(record, e.getMessage());
    }

  }

  private ComparisonSet getComparisonSet(QueueRecord record)
  {
    Optional<ComparisonSet> comparisonSet = managementService.getComparisonSet(record.getComparisonSetKey());
    if (comparisonSet.isEmpty())
      throw new QueueRecordException(record, "ComparisonSet record not found for queue record: " + record);

    return comparisonSet.get();
  }

  private List<ComparisonResult> compare(ComparisonSet comparisonSet, List<ServiceOutput> outputs)
  {
    logger.debug("Comparing outputs: {}", outputs);
    try
    {
      Thread.sleep(500);
    }
    catch (InterruptedException e)
    {
    }
    // TODO:
    List<ComparisonResult> results = new ArrayList<>();
    return results;
  }

  private void saveComparisonResults(ComparisonSet comparisonSet, List<ComparisonResult> results)
  {
    comparisonSet.setComparisonDate(OffsetDateTime.now());
    comparisonSet.setResults(results);
    comparisonSet.setStatus(results.isEmpty() ? Status.PASS : Status.FAIL);
    managementService.saveComparisonSet(comparisonSet);
  }

  private void onComplete(QueueRecord record)
  {
    if (config.isDeleteOnComplete())
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

  private List<ServiceOutput> getServiceOutputs(QueueRecord record)
  {
    List<ServiceOutput> outputs = managementService.getServiceOutput(record.getComparisonSetKey());
    if (outputs.isEmpty())
      throw new QueueRecordException(record, "ServiceOutput records not found for queue record: " + record);
    return outputs;
  }

  public static final class QueueRecordException extends RuntimeException
  {
    private QueueRecord queueRecord;

    QueueRecordException(QueueRecord queueRecord, String message)
    {
      super(message);
      this.queueRecord = queueRecord;
    }

    QueueRecordException(QueueRecord queueRecord, Exception source)
    {
      super(source);
      this.queueRecord = queueRecord;
    }

    public QueueRecord getQueueRecord()
    {
      return queueRecord;
    }
  }
}

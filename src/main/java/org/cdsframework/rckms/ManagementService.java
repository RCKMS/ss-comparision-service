package org.cdsframework.rckms;

import static org.cdsframework.rckms.SSComparisonServiceApplicationConfig.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ComparisonSet.Status;
import org.cdsframework.rckms.dao.ComparisonSetQuery;
import org.cdsframework.rckms.dao.ComparisonSetRepository;
import org.cdsframework.rckms.dao.ComparisonTest;
import org.cdsframework.rckms.dao.ComparisonTestRepository;
import org.cdsframework.rckms.dao.QueueRecord;
import org.cdsframework.rckms.dao.QueueRepository;
import org.cdsframework.rckms.dao.ServiceOutput;
import org.cdsframework.rckms.dao.ServiceOutputRepository;
import org.cdsframework.rckms.rest.AddOutputRequest;
import org.cdsframework.rckms.rest.ComparisonTestSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ManagementService
{

  @Autowired
  private ManagementService self;
  private QueueRepository queueRepo;
  private ComparisonTestRepository comparisonTestRepo;
  private ComparisonSetRepository comparisonSetRepo;
  private ServiceOutputRepository serviceOutputRepo;

  public ManagementService(QueueRepository queueRepo, ComparisonTestRepository comparisonTestRepo,
      ComparisonSetRepository comparisonSetRepo,
      ServiceOutputRepository serviceOutputRepo)
  {
    this.queueRepo = queueRepo;
    this.comparisonTestRepo = comparisonTestRepo;
    this.comparisonSetRepo = comparisonSetRepo;
    this.serviceOutputRepo = serviceOutputRepo;
  }

  public void addServiceOutput(ComparisonTest test, String comparisonSetKey, String sourceId, AddOutputRequest req)
  {
    // 1. add service_output
    ServiceOutput serviceOutput =
        new ServiceOutput(test.getId(), comparisonSetKey, sourceId);
    serviceOutput.setServiceStatus(req.getServiceStatus());
    serviceOutput.setOutput(req.getServiceOutput());
    serviceOutput.setServiceResponseTime(req.getServiceResponseTime());
    serviceOutputRepo.save(serviceOutput);

    // 2. upsert comparison_set doc
    int matchCount = comparisonSetRepo.addOrUpdate(test, comparisonSetKey);

    // If matchCount > 0, then the comparisonSetKey already existed and we did an update instead of insert.
    // If so, then this comparison set is now ready for processing so we can add the queue record
    if (matchCount > 0)
    {
      // 3. Since the status can't be updated to PENDING as part of the upsert above, we update it now
      comparisonSetRepo.markReadyForComparison(comparisonSetKey);
      // 4. Add queue record to indicate we can process this comparison set now
      QueueRecord queueRecord = new QueueRecord(comparisonSetKey);
      queueRepo.save(queueRecord);
    }

  }

  public Optional<ComparisonSet> getComparisonSet(String comparisonSetKey)
  {
    return Optional.ofNullable(comparisonSetRepo.findByComparisonSetKey(comparisonSetKey));
  }

  public Page<ComparisonSet> findComparisonSets(ComparisonSetQuery query)
  {
    return comparisonSetRepo.findComparisonSets(query);
  }

  public List<ServiceOutput> getServiceOutput(ComparisonSet comparisonSet)
  {
    List<ServiceOutput> outputs = null;
    if (Status.PASS.equals(comparisonSet.getStatus()))
    {
      // For ones have been compared and in a PASS state, the ServiceOutput will not have the actual service payload
      // since we delete that for space reasons.
      // So, we first see if we still have the FULL, original ServiceOutput in the service_output collection since that
      // will have all data. But it may have been expired and removed from the collection.
      outputs = loadServiceOutput(comparisonSet.getComparisonSetKey());
      // if the service_output docs have been expired, then we fallback to returning the minimized outputs attached
      // directly to the comparison_set doc
      if (outputs.isEmpty())
        outputs = comparisonSet.getServiceOutputs();
    }
    else
      // If it's not yet been evaluated or was a FAIL, then simply return the output attached to the ComparisonSet.
      outputs = comparisonSet.getServiceOutputs();

    return outputs != null ? outputs : new ArrayList<>();
  }

  public List<ServiceOutput> loadServiceOutput(String comparisonSetKey)
  {
    return serviceOutputRepo.findByComparisonSetKey(comparisonSetKey);
  }

  public void saveComparisonSet(ComparisonSet comparisonSet)
  {
    comparisonSetRepo.save(comparisonSet);
  }

  /**
   * This only updates the fields we allow to be modified
   *
   * @param comparisonSet
   */
  public void updateComparisonSet(ComparisonSet comparisonSet)
  {

  }

  public void saveServiceOutput(ServiceOutput output)
  {
    serviceOutputRepo.save(output);
  }

  public void markServiceOutputsAsCompared(List<ServiceOutput> outputs, OffsetDateTime comparisonDate)
  {
    serviceOutputRepo.markCompared(outputs, comparisonDate);
  }

  public Optional<ComparisonTest> loadComparisonTest(String testId)
  {
    return comparisonTestRepo.findById(testId);
  }

  @Cacheable(cacheNames = CACHE_COMPARISON_TESTS)
  public Optional<ComparisonTest> getComparisonTest(String testId)
  {
    return loadComparisonTest(testId);
  }

  @CachePut(cacheNames = CACHE_COMPARISON_TESTS, key = "#test.id")
  public ComparisonTest addTest(ComparisonTest test)
  {
    test.setCreateDate(OffsetDateTime.now());
    return comparisonTestRepo.save(test);
  }

  public Page<ComparisonTest> getAllTests(Pageable pageable)
  {
    return comparisonTestRepo.findAll(pageable);
  }

  public Optional<ComparisonTestSummary> getTestSummary(String testId, OffsetDateTime start, OffsetDateTime end)
  {
    Optional<ComparisonTest> test = comparisonTestRepo.findById(testId);
    if (test.isEmpty())
      return Optional.empty();

    ComparisonTestSummary summary = new ComparisonTestSummary(testId);
    summary.setStartDate(start);
    summary.setEndDate(end);
    summary.addComparisonSetInfo(
        comparisonSetRepo.statusCounts(testId, start, end),
        comparisonSetRepo.failureTypeCounts(testId, start, end));
    summary.addServiceOutputStats(comparisonSetRepo.serviceOutputStats(testId, start, end));

    return Optional.of(summary);
  }
}

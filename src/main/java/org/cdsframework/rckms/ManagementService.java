package org.cdsframework.rckms;

import static org.cdsframework.rckms.SSComparisonServiceApplicationConfig.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ComparisonSetRepository;
import org.cdsframework.rckms.dao.ComparisonTest;
import org.cdsframework.rckms.dao.ComparisonTestRepository;
import org.cdsframework.rckms.dao.QueueRecord;
import org.cdsframework.rckms.dao.QueueRepository;
import org.cdsframework.rckms.dao.ServiceOutput;
import org.cdsframework.rckms.dao.ServiceOutputRepository;
import org.cdsframework.rckms.rest.AddOutputRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
        new ServiceOutput(test.getId(), comparisonSetKey, sourceId, req.getServiceStatus(), req.getServiceOutput());
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

  public List<ServiceOutput> getServiceOutput(String comparisonSetKey)
  {
    return serviceOutputRepo.findByComparisonSetKey(comparisonSetKey);
  }

  public void saveComparisonSet(ComparisonSet comparisonSet)
  {
    comparisonSetRepo.save(comparisonSet);
  }

  @Cacheable(cacheNames = CACHE_COMPARISON_TESTS)
  public Optional<ComparisonTest> getComparisonTest(String testId)
  {
    return comparisonTestRepo.findById(testId);
  }

  @CachePut(cacheNames = CACHE_COMPARISON_TESTS)
  public ComparisonTest addTest(ComparisonTest test)
  {
    test.setCreateDate(OffsetDateTime.now());
    return comparisonTestRepo.save(test);
  }
}

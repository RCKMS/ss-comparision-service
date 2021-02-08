package org.cdsframework.rckms;

import java.util.List;
import java.util.Optional;

import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ComparisonSetRepository;
import org.cdsframework.rckms.dao.QueueRecord;
import org.cdsframework.rckms.dao.QueueRepository;
import org.cdsframework.rckms.dao.ServiceOutput;
import org.cdsframework.rckms.dao.ServiceOutputRepository;
import org.cdsframework.rckms.rest.AddOutputRequest;
import org.springframework.stereotype.Service;

@Service
public class SSComparisonService
{

  private QueueRepository queueRepo;
  private ComparisonSetRepository comparisonSetRepo;
  private ServiceOutputRepository serviceOutputRepo;

  public SSComparisonService(QueueRepository queueRepo, ComparisonSetRepository comparisonSetRepo,
      ServiceOutputRepository serviceOutputRepo)
  {
    this.queueRepo = queueRepo;
    this.comparisonSetRepo = comparisonSetRepo;
    this.serviceOutputRepo = serviceOutputRepo;
  }

  public void addServiceOutput(String comparisonSetKey, String sourceId, AddOutputRequest req)
  {
    // 1. add service_output
    ServiceOutput serviceOutput = new ServiceOutput(comparisonSetKey, sourceId, req.getServiceStatus(), req.getServiceOutput());
    serviceOutputRepo.save(serviceOutput);

    // 2. upsert comparison_set doc
    int matchCount = comparisonSetRepo.addOrUpdate(comparisonSetKey);
    // If matchCount > 0, then the comparisonSetKey already existed and we did an update instead of insert.
    // If so, then this comparison set is now ready for processing so we can add the queue record
    if (matchCount > 0)
    {
      // 3. Add queue record to indicate we can process this comparison set now
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
}

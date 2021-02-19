package org.cdsframework.rckms.rest;

import org.cdsframework.rckms.QueueProcessingService;
import org.cdsframework.rckms.dao.QueueRecord.QueueStatus;
import org.cdsframework.rckms.dao.QueueRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ss-comparison-service/v1/queue")
@Validated
public class QueueController
{
  private QueueProcessingService queueProcessingService;
  private QueueRepository queueRepository;

  public QueueController(QueueProcessingService queueProcessingService, QueueRepository queueRepository)
  {
    this.queueProcessingService = queueProcessingService;
    this.queueRepository = queueRepository;
  }

  @GetMapping(value = "/stats")
  public ResponseEntity<QueueStatsDTO> getStats()
  {
    QueueStatsDTO stats = new QueueStatsDTO();
    if (queueProcessingService != null)
      stats.setProcessingStats(queueProcessingService.getProcessingStats());

    stats.setPendingCount(queueRepository.countByStatus(QueueStatus.PENDING));

    return (ResponseEntity.ok(stats));
  }

}

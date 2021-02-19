package org.cdsframework.rckms.dao;

import org.cdsframework.rckms.dao.QueueRecord.QueueStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QueueRepository extends MongoRepository<QueueRecord, String>, CustomQueueRepository
{

  Long countByStatus(QueueStatus status);
}

package org.cdsframework.rckms.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface QueueRepository extends MongoRepository<QueueRecord, String>, CustomQueueRepository
{

}

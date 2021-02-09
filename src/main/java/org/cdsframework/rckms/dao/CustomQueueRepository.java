package org.cdsframework.rckms.dao;

import java.util.Optional;

public interface CustomQueueRepository
{
  void addQueueRecord(QueueRecord queueRecord);

  Optional<QueueRecord> takeNextPending();
}

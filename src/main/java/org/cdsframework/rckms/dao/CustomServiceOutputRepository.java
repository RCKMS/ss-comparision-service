package org.cdsframework.rckms.dao;

import java.time.OffsetDateTime;
import java.util.Map;

public interface CustomServiceOutputRepository
{
  Map<String, Integer> countsForTestGroupedBySource(String testId, OffsetDateTime start, OffsetDateTime end);
}

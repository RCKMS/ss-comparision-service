package org.cdsframework.rckms.dao;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.cdsframework.rckms.dao.ComparisonSet.Status;
import org.cdsframework.rckms.dao.CustomComparisonSetRepositoryImpl.ServiceOutputAggregation;
import org.springframework.data.domain.Page;

public interface CustomComparisonSetRepository
{
  int addOrUpdate(ComparisonTest test, String comparisonSetKey);

  int markReadyForComparison(String comparisonSetKey);

  Map<Status, Integer> statusCounts(String comparisonTestId, OffsetDateTime start, OffsetDateTime end);

  Map<ComparisonResult.Type, Integer> failureTypeCounts(String comparisonTestId, OffsetDateTime start, OffsetDateTime end);

  List<ServiceOutputAggregation> serviceOutputStats(String comparisonTestId, OffsetDateTime start, OffsetDateTime end);

  Page<ComparisonSet> findComparisonSets(ComparisonSetQuery query);

}

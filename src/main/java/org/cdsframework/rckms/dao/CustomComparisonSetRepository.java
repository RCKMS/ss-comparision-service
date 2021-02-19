package org.cdsframework.rckms.dao;

public interface CustomComparisonSetRepository
{
  int addOrUpdate(ComparisonTest test, String comparisonSetKey);

  int markReadyForComparison(String comparisonSetKey);
}

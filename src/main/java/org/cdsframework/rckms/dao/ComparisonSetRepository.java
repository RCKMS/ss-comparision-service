package org.cdsframework.rckms.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComparisonSetRepository extends MongoRepository<ComparisonSet, String>, CustomComparisonSetRepository
{
  ComparisonSet findByComparisonSetKey(String comparisonSetKey);
}

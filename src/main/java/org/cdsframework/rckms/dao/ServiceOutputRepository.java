package org.cdsframework.rckms.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServiceOutputRepository extends MongoRepository<ServiceOutput, String>, CustomServiceOutputRepository
{
  List<ServiceOutput> findByComparisonSetKey(String comparisonSetKey);
}

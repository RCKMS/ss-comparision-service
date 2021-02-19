package org.cdsframework.rckms.compare;

import java.util.List;

import org.cdsframework.rckms.dao.ComparisonResult;

public interface ComparisonEngine
{
  String getId();
  
  List<ComparisonResult> compare(ComparisonContext context);
}

package org.cdsframework.rckms.compare;

import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ComparisonTest;

public class ComparisonContext
{
  private ComparisonTest comparisonTest;
  private ComparisonSet comparisonSet;
  private String controlXml;
  private String variantXml;

  public ComparisonContext(ComparisonTest comparisonTest, ComparisonSet comparisonSet,
      String controlXml, String variantXml)
  {
    this.comparisonTest = comparisonTest;
    this.comparisonSet = comparisonSet;
    this.controlXml = controlXml;
    this.variantXml = variantXml;
  }

  public ComparisonTest getComparisonTest()
  {
    return comparisonTest;
  }

  public ComparisonSet getComparisonSet()
  {
    return comparisonSet;
  }

  public String getControlXml()
  {
    return controlXml;
  }

  public String getVariantXml()
  {
    return variantXml;
  }
}

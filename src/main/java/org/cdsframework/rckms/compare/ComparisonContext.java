package org.cdsframework.rckms.compare;

import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ComparisonTest;
import org.cdsframework.rckms.dao.ServiceOutput;

public class ComparisonContext
{
  private ComparisonTest comparisonTest;
  private ComparisonSet comparisonSet;
  private ServiceOutput control;
  private ServiceOutput variant;

  public ComparisonContext(ComparisonTest comparisonTest, ComparisonSet comparisonSet,
      ServiceOutput control, ServiceOutput variant)
  {
    this.comparisonTest = comparisonTest;
    this.comparisonSet = comparisonSet;
    this.control = control;
    this.variant = variant;
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
    return control.getOutput();
  }

  public String getVariantXml()
  {
    return variant.getOutput();
  }

  public ServiceOutput getControl()
  {
    return control;
  }

  public ServiceOutput getVariant()
  {
    return variant;
  }
}

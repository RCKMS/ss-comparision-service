package org.cdsframework.rckms.compare;

import java.util.List;
import java.util.stream.Collectors;

import org.cdsframework.rckms.dao.ComparisonResult;
import org.cdsframework.rckms.dao.ComparisonResult.Type;
import org.cdsframework.rckms.dao.ComparisonTest;
import org.cdsframework.rckms.dao.ServiceOutput;

public class ComparisonResults
{

  static ComparisonResult controlMissing(ComparisonTest test, List<ServiceOutput> outputs)
  {
    String sourceIds = outputs.stream().map(ServiceOutput::getSourceId).collect(Collectors.joining(","));
    String desc =
        String.format("A control output with sourceId '%s' was not found. Available sourceIds: '%s'", test.getControlSourceId(),
            sourceIds);
    return create(Type.CONTROL_MISSING, desc);
  }

  static ComparisonResult variantMissing(ComparisonTest test, List<ServiceOutput> outputs)
  {
    String sourceIds = outputs.stream().map(ServiceOutput::getSourceId).collect(Collectors.joining(","));
    String desc = String.format("A variant output was not found. Available sourceIds: '%s'", sourceIds);
    return create(Type.VARIANT_MISSING, desc);
  }

  static ComparisonResult statusDiff(ComparisonTest test, ServiceOutput control, ServiceOutput variant)
  {
    String desc = String.format("%s=%d; %s=%d", control.getSourceId(), control.getServiceStatus(), variant.getSourceId(),
        variant.getServiceStatus());
    ComparisonResult result = create(Type.STATUS_DIFF, desc);
    result.setControlServiceOutputId(control.getId());
    result.setVariantServiceOutputId(variant.getId());
    return result;
  }

  static ComparisonResult create(Type type, String desc)
  {
    ComparisonResult result = new ComparisonResult(type);
    result.setDescription(desc);
    return result;
  }
}

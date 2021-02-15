package org.cdsframework.rckms.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.cdsframework.rckms.dao.ComparisonResult;
import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ComparisonTest;
import org.cdsframework.rckms.dao.ServiceOutput;

public class Comparison
{

  private ComparisonTest test;
  private ComparisonSet comparisonSet;

  public Comparison(ComparisonTest test, ComparisonSet comparisonSet)
  {
    this.test = test;
    this.comparisonSet = comparisonSet;
  }

  public List<ComparisonResult> compare(List<ServiceOutput> outputs)
  {
    // Control is the first occurrence of an output matching the control sourceID.
    // TODO: What if there are multiple control files - how do we pick one?
    Optional<ServiceOutput> control = outputs.stream()
        .filter(output -> output.getSourceId().equals(test.getControlSourceId()))
        .findFirst();

    if (control.isEmpty())
      return List.of(ComparisonResults.controlMissing(test, outputs));

    // Note that in the normal use case, there should only be 2 outputs: 1 control and 1 variant.
    // However, there could be multiple variants in the case that a service returns an error (e.g. jurisdiction-level error)
    // that is retryable, hence we receive more than one variant. So we don't assume there is only a single variant.
    List<ServiceOutput> variants = new ArrayList<>(outputs);
    variants.remove(control.get());
    if (variants.isEmpty())
      return List.of(ComparisonResults.variantMissing(test, outputs));

    List<ComparisonResult> results = new ArrayList<>();
    variants.forEach(variant -> compare(control.get(), variant, results));

    return results;
  }

  private void compare(ServiceOutput control, ServiceOutput variant, List<ComparisonResult> results)
  {
    if (control.getServiceStatus() != variant.getServiceStatus())
    {
      results.add(ComparisonResults.statusDiff(test, control, variant));
      return;
    }

    new XmlComparison().compare(control.getOutput(), variant.getOutput());
  }
}

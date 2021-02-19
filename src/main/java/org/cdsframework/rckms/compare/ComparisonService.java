package org.cdsframework.rckms.compare;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cdsframework.rckms.ManagementService;
import org.cdsframework.rckms.dao.ComparisonResult;
import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ComparisonSet.Status;
import org.cdsframework.rckms.dao.ComparisonTest;
import org.cdsframework.rckms.dao.ServiceOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Service
public class ComparisonService
{
  private static final Logger logger = LoggerFactory.getLogger(ComparisonService.class);

  private ManagementService managementService;
  private Map<String, ComparisonEngine> comparisonEngines;

  public ComparisonService(ManagementService managementService, List<ComparisonEngine> engines)
  {
    this.managementService = managementService;
    comparisonEngines = engines.stream().collect(Collectors.toMap(ComparisonEngine::getId, Function.identity()));
  }

  public List<ComparisonResult> runComparison(String comparisonSetKey)
  {
    StopWatch sw = new StopWatch(String.format("runComparison(%s)", comparisonSetKey));
    sw.start(String.format("getComparisonSet(%s)", comparisonSetKey));
    ComparisonSet comparisonSet = getComparisonSet(comparisonSetKey);
    sw.stop();
    sw.start(String.format("getServiceOutputs(%s)", comparisonSetKey));
    List<ServiceOutput> outputs = getServiceOutputs(comparisonSetKey);
    sw.stop();
    sw.start(String.format("getComparisonTest(%s)", comparisonSetKey));
    ComparisonTest test = getComparisonTest(comparisonSet);
    sw.stop();

    sw.start(String.format("Comparing output (%s)", comparisonSetKey));
    if (logger.isTraceEnabled())
      logger.trace("Running comparison: testId={}; comparisonSetKey{}; outputCount={}", test.getId(), comparisonSetKey,
          outputs.size());

    List<ComparisonResult> results = new ArrayList<>();

    // Control is the first occurrence of an output matching the control sourceID.
    // TODO: What if there are multiple control files - how do we pick one?
    Optional<ServiceOutput> control = outputs.stream()
        .filter(output -> output.getSourceId().equals(test.getControlSourceId()))
        .findFirst();

    if (control.isEmpty())
      results.add(ComparisonResults.controlMissing(test, outputs));
    else
    {
      // Note that in the normal use case, there should only be 2 outputs: 1 control and 1 variant.
      // However, there could be multiple variants in the case that a service returns an error (e.g. jurisdiction-level error)
      // that is retryable, hence we receive more than one variant. So we don't assume there is only a single variant.
      List<ServiceOutput> variants = new ArrayList<>(outputs);
      variants.remove(control.get());
      if (variants.isEmpty())
        results.add(ComparisonResults.variantMissing(test, outputs));

      variants.forEach(variant -> compare(test, comparisonSet, control.get(), variant, results));
    }
    sw.stop();

    sw.start(String.format("saveComparisonResults(%s)", comparisonSetKey));
    saveComparisonResults(comparisonSet, results);
    if (logger.isTraceEnabled())
      logger.trace("Comparison complete: testId={}; comparisonSetKey{}; errorCount={}", test.getId(), comparisonSetKey,
          results.size());

    sw.stop();
    if (logger.isTraceEnabled())
      logger.trace(sw.prettyPrint());
    return results;
  }

  private void compare(ComparisonTest test, ComparisonSet comparisonSet, ServiceOutput control, ServiceOutput variant,
      List<ComparisonResult> results)
  {
    ComparisonContext context = new ComparisonContext(test, comparisonSet, control.getOutput(), variant.getOutput());
    if (control.getServiceStatus() != variant.getServiceStatus())
    {
      results.add(ComparisonResults.statusDiff(test, control, variant));
      return;
    }

    results.addAll(getComparisonEngine(context).compare(context));
  }

  private ComparisonEngine getComparisonEngine(ComparisonContext context)
  {
    String engineId = context.getComparisonTest().getComparisonEngineId();
    ComparisonEngine engine = comparisonEngines.get(engineId);
    if (engine == null)
      throw new ComparisonServiceException(context.getComparisonSet().getComparisonSetKey(),
          "ComparisonEngine '" + engineId + "' not found for test '" + context.getComparisonTest().getId() + "'");
    return engine;
  }

  private ComparisonTest getComparisonTest(ComparisonSet comparisonSet)
  {
    Optional<ComparisonTest> test = managementService.getComparisonTest(comparisonSet.getComparisonTestId());
    if (test.isEmpty())
      throw new ComparisonServiceException(comparisonSet.getComparisonSetKey(),
          "ComparisonTest record not found: " + comparisonSet.getComparisonTestId());
    return test.get();
  }

  private List<ServiceOutput> getServiceOutputs(String comparisonSetKey)
  {
    List<ServiceOutput> outputs = managementService.getServiceOutput(comparisonSetKey);
    if (outputs.isEmpty())
      throw new ComparisonServiceException(comparisonSetKey,
          "ServiceOutput records not found for comparisonSetKey: " + comparisonSetKey);
    return outputs;
  }

  private ComparisonSet getComparisonSet(String comparisonSetKey)
  {
    Optional<ComparisonSet> comparisonSet = managementService.getComparisonSet(comparisonSetKey);
    if (comparisonSet.isEmpty())
      throw new ComparisonServiceException(comparisonSetKey,
          "ComparisonSet record not found for comparisonSetKey: " + comparisonSetKey);

    return comparisonSet.get();
  }

  private void saveComparisonResults(ComparisonSet comparisonSet, List<ComparisonResult> results)
  {
    comparisonSet.setComparisonDate(OffsetDateTime.now());
    comparisonSet.setResults(results);
    comparisonSet.setStatus(results.isEmpty() ? Status.PASS : Status.FAIL);
    managementService.saveComparisonSet(comparisonSet);
  }

  public static final class ComparisonServiceException extends RuntimeException
  {
    private String comparisonSetKey;

    ComparisonServiceException(String comparisonSetKey, String message)
    {
      super(message);
      this.comparisonSetKey = comparisonSetKey;
    }

    ComparisonServiceException(String comparisonSetKey, Exception source)
    {
      super(source);
      this.comparisonSetKey = comparisonSetKey;
    }

    public String getComparisonSetKey()
    {
      return comparisonSetKey;
    }
  }
}

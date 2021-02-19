package org.cdsframework.rckms.rest;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.cdsframework.rckms.ManagementService;
import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ComparisonTest;
import org.cdsframework.rckms.dao.ServiceOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/ss-comparison-service/v1")
@Validated
public class ComparisonController
{
  private ManagementService managementService;

  public ComparisonController(ManagementService managementService)
  {
    this.managementService = managementService;
  }

  @PostMapping(value = "/comparison-tests/{comparison-test}/comparison-sets/{comparison-key}/output/{source-id}")
  public ResponseEntity<Void> addOutput(
      @PathVariable(name = "comparison-test") String comparisonTestId,
      @PathVariable(name = "comparison-key") String comparisonKey,
      @PathVariable(name = "source-id") String sourceId,
      @Valid @RequestBody AddOutputRequest req)
  {
    Optional<ComparisonTest> test = managementService.getComparisonTest(comparisonTestId);
    if (test.isEmpty())
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("ComparisonTest '%s' not found.", comparisonTestId));

    managementService.addServiceOutput(test.get(), comparisonKey, sourceId, req);
    return ResponseEntity.accepted().build();
  }

  @GetMapping(value = "/comparison-sets/{comparison-key}")
  public ResponseEntity<ComparisonSet> getComparisonSet(@PathVariable(name = "comparison-key") String comparisonKey)
  {
    Optional<ComparisonSet> result = managementService.getComparisonSet(comparisonKey);
    if (result.isEmpty())
      return ResponseEntity.notFound().build();

    return (ResponseEntity.ok(result.get()));
  }

  @GetMapping(value = "/comparison-sets/{comparison-key}/output")
  public ResponseEntity<List<ServiceOutput>> getServiceOutput(@PathVariable(name = "comparison-key") String comparisonKey)
  {
    List<ServiceOutput> result = managementService.getServiceOutput(comparisonKey);
    return (ResponseEntity.ok(result));
  }
}

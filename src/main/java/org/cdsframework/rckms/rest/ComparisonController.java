package org.cdsframework.rckms.rest;

import java.util.Optional;

import javax.validation.Valid;

import org.cdsframework.rckms.ManagementService;
import org.cdsframework.rckms.dao.ComparisonTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
}

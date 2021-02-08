package org.cdsframework.rckms.rest;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.cdsframework.rckms.SSComparisonService;
import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ServiceOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ss-comparison-service/v1/comparison-sets")
@Validated
public class ComparisonSetController
{
  private SSComparisonService comparisonService;

  public ComparisonSetController(SSComparisonService comparisonService)
  {
    this.comparisonService = comparisonService;
  }

  @PostMapping(value = "/{comparison-key}/output/{source-id}")
  public ResponseEntity<Void> addOutput(
      @PathVariable(name = "comparison-key") String comparisonKey,
      @PathVariable(name = "source-id") String sourceId,
      @Valid @RequestBody AddOutputRequest req)
  {
    comparisonService.addServiceOutput(comparisonKey, sourceId, req);
    return ResponseEntity.accepted().build();
  }

  @GetMapping(value = "/{comparison-key}")
  public ResponseEntity<ComparisonSet> getComparisonSet(@PathVariable(name = "comparison-key") String comparisonKey)
  {
    Optional<ComparisonSet> result = comparisonService.getComparisonSet(comparisonKey);
    if (result.isEmpty())
      return ResponseEntity.notFound().build();

    return (ResponseEntity.ok(result.get()));
  }

  @GetMapping(value = "/{comparison-key}/output")
  public ResponseEntity<List<ServiceOutput>> getServiceOutput(@PathVariable(name = "comparison-key") String comparisonKey)
  {
    List<ServiceOutput> result = comparisonService.getServiceOutput(comparisonKey);
    return (ResponseEntity.ok(result));
  }
}

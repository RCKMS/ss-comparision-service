package org.cdsframework.rckms.rest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.cdsframework.rckms.ManagementService;
import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ComparisonSet.Status;
import org.cdsframework.rckms.dao.ComparisonSetQuery;
import org.cdsframework.rckms.dao.ComparisonTest;
import org.cdsframework.rckms.dao.ServiceOutput;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ss-comparison-service/v1/management")
@Validated
public class ManagementController
{
  private ManagementService managementService;

  public ManagementController(ManagementService managementService)
  {
    this.managementService = managementService;
  }

  @GetMapping(value = "/comparison-sets/{comparison-key}")
  public ResponseEntity<ComparisonSet> getComparisonSet(@PathVariable(name = "comparison-key") String comparisonKey)
  {
    Optional<ComparisonSet> result = managementService.getComparisonSet(comparisonKey);
    if (result.isEmpty())
      return ResponseEntity.notFound().build();

    return (ResponseEntity.ok(result.get()));
  }

  @GetMapping(value = "/comparison-tests/{comparison-test}/comparison-sets")
  public ResponseEntity<Page<ComparisonSet>> findComparisonSets(
      @PathVariable(name = "comparison-test") String comparisonTestId,
      @RequestParam(name = "status", required = false) Status status,
      @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          OffsetDateTime startDate,
      @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
      Pageable pageable
  )
  {
    Optional<ComparisonTest> test = managementService.getComparisonTest(comparisonTestId);
    if (test.isEmpty())
      return ResponseEntity.notFound().build();

    ComparisonSetQuery query = new ComparisonSetQuery(comparisonTestId, pageable)
        .withStatus(status)
        .onOrAfter(startDate)
        .onOrBefore(endDate);
    Page<ComparisonSet> results = managementService.findComparisonSets(query);
    return ResponseEntity.ok(results);
  }

  @GetMapping(value = "/comparison-sets/{comparison-key}/output")
  public ResponseEntity<List<ServiceOutput>> getServiceOutput(@PathVariable(name = "comparison-key") String comparisonKey)
  {
    List<ServiceOutput> result = managementService.getServiceOutput(comparisonKey);
    return (ResponseEntity.ok(result));
  }

  @GetMapping(value = "/comparison-tests/{comparison-test}/summary")
  public ResponseEntity<ComparisonTestSummary> getTestSummary(
      @PathVariable(name = "comparison-test") String comparisonTestId,
      @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          OffsetDateTime startDate,
      @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate)
  {
    Optional<ComparisonTestSummary> result = managementService.getTestSummary(comparisonTestId, startDate, endDate);
    if (result.isEmpty())
      return ResponseEntity.notFound().build();
    return (ResponseEntity.ok(result.get()));
  }
}

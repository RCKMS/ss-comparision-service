package org.cdsframework.rckms.rest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.cdsframework.rckms.ManagementService;
import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ComparisonSet.Status;
import org.cdsframework.rckms.dao.ComparisonSetQuery;
import org.cdsframework.rckms.dao.ComparisonTest;
import org.cdsframework.rckms.dao.ServiceOutput;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

  @PutMapping(value = "/comparison-sets/{comparison-key}")
  public ResponseEntity<Void> saveComparisonSet(
      @PathVariable(name = "comparison-key") String comparisonKey,
      @RequestBody ComparisonSet comparisonSet)
  {
    Optional<ComparisonSet> result = managementService.getComparisonSet(comparisonKey);
    if (result.isEmpty())
      return ResponseEntity.notFound().build();

    managementService.saveComparisonSet(comparisonSet);
    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "/comparison-sets/{comparison-key}/output")
  public ResponseEntity<List<ServiceOutput>> getServiceOutput(@PathVariable(name = "comparison-key") String comparisonKey)
  {
    Optional<ComparisonSet> comparisonSet = managementService.getComparisonSet(comparisonKey);
    if (comparisonSet.isEmpty())
      return ResponseEntity.notFound().build();

    List<ServiceOutput> result = managementService.getServiceOutput(comparisonSet.get());
    return (ResponseEntity.ok(result));
  }

  @GetMapping(value = "/comparison-sets/{comparison-key}/output/{output-id}")
  public ResponseEntity<ServiceOutput> getServiceOutput(@PathVariable(name = "comparison-key") String comparisonKey,
      @PathVariable(name = "output-id") String id)
  {
    ServiceOutput output = getRequiredServiceOutput(comparisonKey, id);
    return (ResponseEntity.ok(output));
  }

  @GetMapping(value = "/comparison-sets/{comparison-key}/output/{output-id}/xml")
  @RequestMapping(value = "/comparison-sets/{comparison-key}/output/{output-id}/xml", method = RequestMethod.GET,
                  produces = "application/xml")
  public ResponseEntity<String> getServiceOutputXml(@PathVariable(name = "comparison-key") String comparisonKey,
      @PathVariable(name = "output-id") String id)
  {
    ServiceOutput output = getRequiredServiceOutput(comparisonKey, id);
    return (ResponseEntity.ok(output.getOutput()));
  }

  private ServiceOutput getRequiredServiceOutput(String comparisonSetKey, String outputId)
  {
    ComparisonSet comparisonSet = managementService.getComparisonSet(comparisonSetKey)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, comparisonSetKey));
    return managementService.getServiceOutput(comparisonSet).stream()
        .filter(so -> so.getId().equals(outputId))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, outputId));
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

  @GetMapping(value = "/comparison-tests")
  public ResponseEntity<Page<ComparisonTest>> getAllComparisonTests(Pageable pageable)
  {
    return ResponseEntity.ok(managementService.getAllTests(pageable));
  }

  @GetMapping(value = "/comparison-tests/{comparison-test-id}")
  public ResponseEntity<ComparisonTest> getTestSummary(@PathVariable(name = "comparison-test-id") String comparisonTestId)
  {
    Optional<ComparisonTest> test = managementService.loadComparisonTest(comparisonTestId);
    if (test.isEmpty())
      return ResponseEntity.notFound().build();
    return ResponseEntity.ok(test.get());
  }

  @PostMapping(value = "/comparison-tests")
  public ResponseEntity<ComparisonTest> addComparisonTest(@Valid @RequestBody ComparisonTest comparisonTest)
  {
    Optional<ComparisonTest> existingTest = managementService.getComparisonTest(comparisonTest.getId());
    if (existingTest.isPresent())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ComparisonTest '" + comparisonTest.getId() + "' already exists.");

    return ResponseEntity.ok(managementService.addTest(comparisonTest));
  }
}

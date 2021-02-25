package org.cdsframework.rckms.compare;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.cdsframework.rckms.ManagementService;
import org.cdsframework.rckms.dao.ComparisonResult;
import org.cdsframework.rckms.dao.ComparisonResult.Type;
import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ComparisonSet.Status;
import org.cdsframework.rckms.dao.ComparisonTest;
import org.cdsframework.rckms.dao.ServiceOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ComparisonServiceTest
{
  @Test
  public void testRunComparison_withNoDiffs()
  {
    String compSetKey = "key";
    ComparisonTest test = mockComparisonTest("test", "control");
    ComparisonSet compSet = mockComparisonSet("test", compSetKey);
    List<ServiceOutput> outputs = List.of(
        mockServiceOutput(test.getId(), compSetKey, "control", 200, "<root/>"),
        mockServiceOutput(test.getId(), compSetKey, "variant", 200, "<root/>")
    );
    ManagementService mgmtSvc = mockManagementService(test, compSet, outputs);
    ComparisonEngine engine = mockEngine("default");

    ComparisonService compSvc = createComparisonService(mgmtSvc, engine);
    assertTrue(compSvc.runComparison(compSetKey).isEmpty());
    assertEquals(Status.PASS, compSet.getStatus());
    assertTrue(compSet.getResults().isEmpty());
    verifySave(mgmtSvc, compSet);
  }

  @Test
  public void testRunComparison_withMissingControl()
  {
    String compSetKey = "key";
    ComparisonTest test = mockComparisonTest("test", "control");
    ComparisonSet compSet = mockComparisonSet("test", compSetKey);
    List<ServiceOutput> outputs = List.of(
        mockServiceOutput(test.getId(), compSetKey, "variant", 200, "<root/>")
    );
    ManagementService mgmtSvc = mockManagementService(test, compSet, outputs);
    ComparisonEngine engine = mockEngine("default");

    ComparisonService compSvc = createComparisonService(mgmtSvc, engine);
    List<ComparisonResult> results = compSvc.runComparison(compSetKey);
    assertEquals(1, results.size());
    assertEquals(Type.CONTROL_MISSING, results.get(0).getType());
    assertEquals(Status.FAIL, compSet.getStatus());
    assertEquals(1, compSet.getResults().size());
    assertEquals(Type.CONTROL_MISSING, compSet.getResults().get(0).getType());
    verifySave(mgmtSvc, compSet);
  }

  @Test
  public void testRunComparison_withMissingVariant()
  {
    String compSetKey = "key";
    ComparisonTest test = mockComparisonTest("test", "control");
    ComparisonSet compSet = mockComparisonSet("test", compSetKey);
    List<ServiceOutput> outputs = List.of(
        mockServiceOutput(test.getId(), compSetKey, "control", 200, "<root/>")
    );
    ManagementService mgmtSvc = mockManagementService(test, compSet, outputs);
    ComparisonEngine engine = mockEngine("default");

    ComparisonService compSvc = createComparisonService(mgmtSvc, engine);
    List<ComparisonResult> results = compSvc.runComparison(compSetKey);
    assertEquals(1, results.size());
    assertEquals(Type.VARIANT_MISSING, results.get(0).getType());

    assertEquals(Status.FAIL, compSet.getStatus());
    assertEquals(1, compSet.getResults().size());
    assertEquals(Type.VARIANT_MISSING, compSet.getResults().get(0).getType());
    verifySave(mgmtSvc, compSet);
  }

  private ComparisonService createComparisonService(ManagementService mgmtSvc, ComparisonEngine engine)
  {
    return createComparisonService(mgmtSvc, List.of(engine));
  }

  private ComparisonService createComparisonService(ManagementService mgmtSvc, List<ComparisonEngine> engines)
  {
    return new ComparisonService(mgmtSvc, engines);
  }

  private static ComparisonEngine mockEngine(String id)
  {
    ComparisonEngine engine = Mockito.mock(ComparisonEngine.class);
    when(engine.getId()).thenReturn(id);
    when(engine.compare(Mockito.isA(ComparisonContext.class))).thenReturn(new ArrayList<>());
    return engine;
  }

  private static ComparisonEngine mockEngine(String id, List<ComparisonResult> returnResults)
  {
    ComparisonEngine engine = Mockito.mock(ComparisonEngine.class);
    when(engine.getId()).thenReturn(id);
    when(engine.compare(Mockito.isA(ComparisonContext.class))).thenReturn(returnResults);
    return engine;
  }

  private static ManagementService mockManagementService(ComparisonTest test, ComparisonSet comparisonSet,
      List<ServiceOutput> svcOutput)
  {
    ManagementService svc = Mockito.mock(ManagementService.class);
    when(svc.getComparisonSet(comparisonSet.getComparisonSetKey())).thenReturn(Optional.of(comparisonSet));
    when(svc.getComparisonTest(test.getId())).thenReturn(Optional.of(test));
    when(svc.loadServiceOutput(comparisonSet.getComparisonSetKey())).thenReturn(svcOutput);
    return svc;
  }

  private static void verifySave(ManagementService mgmtSvc, ComparisonSet compSet)
  {
    verify(mgmtSvc, times(1)).saveComparisonSet(compSet);
  }

  private static ComparisonSet mockComparisonSet(String testId, String key)
  {
    ComparisonSet comparisonSet = new ComparisonSet();
    comparisonSet.setComparisonSetKey(key);
    comparisonSet.setComparisonTestId(testId);
    return comparisonSet;
  }

  private static ComparisonTest mockComparisonTest(String testId, String controlSourceId)
  {
    return mockComparisonTest(testId, controlSourceId, "default");
  }

  private static ComparisonTest mockComparisonTest(String testId, String controlSourceId, String engineId)
  {
    ComparisonTest test = new ComparisonTest(testId, controlSourceId);
    test.setComparisonEngineId(engineId);
    return test;
  }

  private static ServiceOutput mockServiceOutput(String testId, String compSetKey, String sourceId, int serviceStatus, String xml)
  {
    ServiceOutput output = new ServiceOutput(testId, compSetKey, sourceId, serviceStatus, xml);
    return output;
  }
}

package org.cdsframework.rckms;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.cdsframework.rckms.dao.ComparisonTest;
import org.cdsframework.rckms.rest.AddOutputRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK,
                properties = "processor.enabled=false")
@ActiveProfiles("sandbox")
public class BulkInsert
{
  @Autowired
  private ManagementService managementService;

  @Test
  public void bulkInsert() throws Exception
  {
    String testId = "test1";
    String controlSourceId = "sourceA";
    String variantSourceId = "sourceB";
    ComparisonTest test = managementService.getComparisonTest(testId).orElse(null);
    if (test == null)
    {
      test = new ComparisonTest(testId, controlSourceId);
      test = managementService.addTest(test);
    }
    ComparisonTest test1 = test;

    ExecutorService executor = Executors.newFixedThreadPool(10);
    for (int i = 0; i < 2; i++)
    {
      executor.submit(() ->
      {
        String comparisonSetKey = UUID.randomUUID().toString();
        AddOutputRequest req = new AddOutputRequest();
        req.setServiceStatus(200);
        req.setServiceOutput("<test/>");
        managementService.addServiceOutput(test1, comparisonSetKey, controlSourceId, req);
        managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, req);
      });
    }
    executor.shutdown();
    executor.awaitTermination(60, TimeUnit.SECONDS);
  }

}

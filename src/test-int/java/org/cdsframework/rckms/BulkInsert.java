package org.cdsframework.rckms;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.cdsframework.rckms.compare.xmlunit.XmlUtils;
import org.cdsframework.rckms.dao.ComparisonTest;
import org.cdsframework.rckms.rest.AddOutputRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK,
                properties = "processor.enabled=false")
@ActiveProfiles("sandbox")
public class BulkInsert
{
  private static final Logger logger = LoggerFactory.getLogger(BulkInsert.class);

  @Autowired
  private ManagementService managementService;

  @Test
  public void test()
  {

  }

  @Test
  @Disabled
  public void bulkInsert() throws Exception
  {
    String testId = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    String engineId = "202103";
    String controlSourceId = "sourceA";
    String variantSourceId = "sourceB";
    ComparisonTest test = managementService.getComparisonTest(testId).orElse(null);
    if (test == null)
    {
      test = new ComparisonTest(testId, controlSourceId);
      test.setComparisonEngineId(engineId);
      test = managementService.addTest(test);
    }
    ComparisonTest test1 = test;

    ClassLoader cl = this.getClass().getClassLoader();
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
    Resource[] resources = resolver.getResources("classpath*:/*.xml");
    //    for (Resource resource : resources)
    //    {
    //      resource.getFilename();
    //    }

    ExecutorService executor = Executors.newFixedThreadPool(10);
    for (int i = 0; i < resources.length; i++)
    {
      final int j = i;
      executor.submit(() ->
      {
        String comparisonSetKey = UUID.randomUUID().toString();
        AddOutputRequest req = new AddOutputRequest();
        req.setServiceStatus(200);
        req.setServiceOutput(XmlUtils.asString(resources[j]));
        if (j % 16 == 0)
        {
          // duplicate some requests
          managementService.addServiceOutput(test1, comparisonSetKey, controlSourceId, req);
          managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, req);
          managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, req);
        }
        else
          if (j % 43 == 0)
          {
            // missing variant
            managementService.addServiceOutput(test1, comparisonSetKey, controlSourceId, req);
          }
          else
            if (j % 41 == 0)
            {
              // missing control
              managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, req);
            }

            else
              if (j % 31 == 0)
              {
                // difference
                managementService.addServiceOutput(test1, comparisonSetKey, controlSourceId, req);
                req.setServiceOutput(req.getServiceOutput().replace("2.16.840.1.114222.4.5.274", "2.16.840.1.114222.4.5.275"));
                managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, req);
              }
              else
              {
                // Normal match
                managementService.addServiceOutput(test1, comparisonSetKey, controlSourceId, req);
                managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, req);
              }

      });

    }
    logger.info("Submitted {} resources for testId {}", resources.length, testId);
    executor.shutdown();
    executor.awaitTermination(60, TimeUnit.SECONDS);
  }

}

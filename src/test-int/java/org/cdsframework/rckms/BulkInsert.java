package org.cdsframework.rckms;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
  @Disabled
  public void demoInsert() throws Exception
  {
    String testId = "20210317_demo_000";
    String engineId = "202103";
    String controlSourceId = "RCKMS_OLD";
    String variantSourceId = "RCKMS_NEW";
    ComparisonTest test = managementService.getComparisonTest(testId).orElse(null);
    if (test == null)
    {
      test = new ComparisonTest(testId, controlSourceId);
      test.setComparisonEngineId(engineId);
      test = managementService.addTest(test);
    }
    ComparisonTest test1 = test;

    String controlXml = XmlUtils.loadXml("classpath:202103_training_TC-Per_DD.xml");
    String variantXml = XmlUtils.loadXml("classpath:202103_demo_TC-Per_DD.xml");

    int count = 100;

    ExecutorService executor = Executors.newFixedThreadPool(15);
    for (int i = 0; i < count; i++)
    {
      final int j = i;
      executor.submit(() ->
      {
        String comparisonSetKey = UUID.randomUUID().toString();
        AddOutputRequest controlReq = new AddOutputRequest();
        controlReq.setServiceStatus(200);
        controlReq.setServiceOutput(controlXml);

        AddOutputRequest variantReq = new AddOutputRequest();
        variantReq.setServiceStatus(200);
        variantReq.setServiceOutput(variantXml);

        if (j >= 52 && j % 52 == 0)
        {
          // duplicate some requests
          managementService.addServiceOutput(test1, comparisonSetKey, controlSourceId, controlReq);
          managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, variantReq);
          managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, variantReq);
        }
        else if (j >= 83 && j % 83 == 0)
        {
          // missing variant
          managementService.addServiceOutput(test1, comparisonSetKey, controlSourceId, controlReq);
        }
        else if (j >= 65 && j % 65 == 0)
        {
          // missing control
          managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, variantReq);
        }

        else if (j >= 60 && j % 60 == 0)
        {
          // difference
          managementService.addServiceOutput(test1, comparisonSetKey, controlSourceId, controlReq);
          variantReq.setServiceOutput(variantReq.getServiceOutput()
              .replace("<reportingTimeframe unit=\"Day(s)\" value=\"3\"/>", "<reportingTimeframe unit=\"Day(s)\" value=\"4\"/>"));
          managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, variantReq);
        }
        else if (j >= 77 && j % 77 == 0)
        {
          // service status difference
          managementService.addServiceOutput(test1, comparisonSetKey, controlSourceId, controlReq);
          variantReq.setServiceStatus(500);
          managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, variantReq);
        }
        else
        {
          // Normal match
          addServiceOutput(test1, comparisonSetKey, controlSourceId, controlReq);
          addServiceOutput(test1, comparisonSetKey, variantSourceId, variantReq);
        }

      });

    }
    logger.info("Submitted {} resources for testId {}", count, testId);
    executor.shutdown();
    executor.awaitTermination(120, TimeUnit.SECONDS);
  }

  @Test
  @Disabled
  public void bulkInsert() throws Exception
  {
    String testId = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    String engineId = "202103";
    String controlSourceId = "RCKMS_OLD";
    String variantSourceId = "RCKMS_NEW";
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

    List<Resource> batch = new ArrayList<>();
    // Duplicate if needed to make a large batch
    for (int i = 0; i < 1; i++)
      batch.addAll(Arrays.asList(resources));

    int count = batch.size();

    ExecutorService executor = Executors.newFixedThreadPool(15);
    for (int i = 0; i < count; i++)
    {
      final int j = i;
      executor.submit(() ->
      {
        String comparisonSetKey = UUID.randomUUID().toString();
        AddOutputRequest req = new AddOutputRequest();
        req.setServiceStatus(200);
        req.setServiceOutput(XmlUtils.asString(batch.get(j)));
        if (j >= 16 && j % 16 == 0)
        {
          // duplicate some requests
          managementService.addServiceOutput(test1, comparisonSetKey, controlSourceId, req);
          managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, req);
          managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, req);
        }
        else if (j >= 43 && j % 43 == 0)
        {
          // missing variant
          managementService.addServiceOutput(test1, comparisonSetKey, controlSourceId, req);
        }
        else if (j >= 41 && j % 41 == 0)
        {
          // missing control
          managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, req);
        }

        else if (j >= 31 && j % 31 == 0)
        {
          // difference
          managementService.addServiceOutput(test1, comparisonSetKey, controlSourceId, req);
          req.setServiceOutput(req.getServiceOutput().replace("2.16.840.1.114222.4.5.274", "2.16.840.1.114222.4.5.275"));
          managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, req);
        }
        else if (j >= 29 && j % 29 == 0)
        {
          // service status difference
          managementService.addServiceOutput(test1, comparisonSetKey, controlSourceId, req);
          req.setServiceStatus(500);
          managementService.addServiceOutput(test1, comparisonSetKey, variantSourceId, req);
        }
        else
        {
          // Normal match
          addServiceOutput(test1, comparisonSetKey, controlSourceId, req);
          addServiceOutput(test1, comparisonSetKey, variantSourceId, req);
        }

      });

    }
    logger.info("Submitted {} resources for testId {}", count, testId);
    executor.shutdown();
    executor.awaitTermination(120, TimeUnit.SECONDS);
  }

  private void addServiceOutput(ComparisonTest test, String compSetKey, String sourceId, AddOutputRequest req)
  {
    try
    {
      managementService.addServiceOutput(test, compSetKey, sourceId, req);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

}

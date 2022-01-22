package org.cdsframework.rckms.util;

import static org.junit.jupiter.api.Assertions.*;

import org.cdsframework.rckms.compare.xmlunit.XmlUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public class ServiceOutputSanitizerTest
{
  private static final Logger logger = LoggerFactory.getLogger(ServiceOutputSanitizerTest.class);

  @Test
  public void test() throws Exception
  {
    ServiceOutputSanitizer sanitizer = new ServiceOutputSanitizer();
    String xml = XmlUtils.loadXml("classpath:ServiceOutputSanitizerTest_output1.xml");
    String sanitizedXml = sanitizer.sanitize(xml);
    logger.info(sanitizedXml);
    assertTrue(sanitizedXml.contains("jurisdiction id=\"sc\""));
    assertFalse(sanitizedXml.contains("<output>"));
    assertFalse(sanitizedXml.contains("START OUTPUT"));

    assertFalse(sanitizedXml.contains("<input>"));
    assertFalse(sanitizedXml.contains("START INPUT"));

    StopWatch sw = new StopWatch();
    sw.start();
    sanitizedXml = sanitizer.sanitize(xml);
    sw.stop();
    logger.info("Sanitized XML in " + sw.getTotalTimeMillis() + "ms");
  }
}

package org.cdsframework.rckms.compare;

import static java.nio.charset.StandardCharsets.*;

import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

public class XmlComparisonTest
{
  @Test
  public void test() throws Exception
  {
    String controlXml = loadXml("classpath:demo_MA.xml");
    String variantXml = loadXml("classpath:demo_MA_2.xml");
    XmlComparison comparison = new XmlComparison();
    comparison.compare(controlXml, variantXml);
  }

  @Test
  public void test2() throws Exception
  {
    String controlXml = loadXml("classpath:1a_RCKMSOutput_PerDx_UT_eICR.xml");
    String variantXml = loadXml("classpath:1c_RCKMSOutput_PerDx_UT_eICR.xml");
    XmlComparison comparison = new XmlComparison();
    comparison.compare(controlXml, variantXml);
  }

  private String loadXml(String resourceUri) throws Exception
  {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource(resourceUri);
    return asString(resource); //("1a_RCKMSOutput_PerDx_UT_eICR.xml")
  }

  public static String asString(Resource resource) throws Exception
  {
    try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8))
    {
      return FileCopyUtils.copyToString(reader);
    }
  }
}

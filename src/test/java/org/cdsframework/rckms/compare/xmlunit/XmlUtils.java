package org.cdsframework.rckms.compare.xmlunit;

import static java.nio.charset.StandardCharsets.*;

import java.io.InputStreamReader;
import java.io.Reader;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

public class XmlUtils
{
  public static String loadXml(String resourceUri) throws Exception
  {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource(resourceUri);
    return asString(resource); //("1a_RCKMSOutput_PerDx_UT_eICR.xml")
  }

  public static String asString(Resource resource)
  {
    try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8))
    {
      return FileCopyUtils.copyToString(reader);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }
}

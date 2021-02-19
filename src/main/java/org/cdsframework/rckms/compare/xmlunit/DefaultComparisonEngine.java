package org.cdsframework.rckms.compare.xmlunit;

import org.springframework.stereotype.Component;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;

@Component
public class DefaultComparisonEngine extends AbstractXmlUnitComparisonEngine
{
  @Override
  public String getId()
  {
    return "default";
  }

  @Override
  protected DiffBuilder createDiffBuilder(String controlXml, String testXml)
  {
    return
        DiffBuilder.compare(Input.fromString(controlXml))
            .withTest(Input.fromString(testXml))
            .checkForSimilar()
            .ignoreWhitespace()
            .ignoreComments();
  }

}

package org.cdsframework.rckms.compare.xmlunit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.cdsframework.rckms.compare.ComparisonContext;
import org.cdsframework.rckms.dao.ComparisonResult;
import org.cdsframework.rckms.dao.ComparisonResult.Type;
import org.cdsframework.rckms.dao.ServiceOutput;
import org.junit.jupiter.api.Test;

public class XmlUnitComparisonEngine202103Test
{
  @Test
  public void testIdentical() throws Exception
  {
    ComparisonContext ctx = createContext("classpath:202103_control_MA.xml", "classpath:202103_control_MA.xml");
    XmlUnitComparisonEngine202103 comparison = new XmlUnitComparisonEngine202103();
    assertTrue(comparison.compare(ctx).isEmpty());
  }

  @Test
  public void testIgnoreKnownDifferences() throws Exception
  {
    // This variant contains diagnostics element and an extra routingEntity element
    ComparisonContext ctx = createContext("classpath:202103_control_MA.xml", "classpath:202103_variant_MA.xml");
    XmlUnitComparisonEngine202103 comparison = new XmlUnitComparisonEngine202103();
    List<ComparisonResult> results = comparison.compare(ctx);
    assertTrue(results.isEmpty(), results.toString());
  }

  @Test
  public void testValidDifference() throws Exception
  {
    // This variant contains a different responseCode
    ComparisonContext ctx = createContext("classpath:202103_control_MA.xml", "classpath:202103_variant_MA_with_diff.xml");
    XmlUnitComparisonEngine202103 comparison = new XmlUnitComparisonEngine202103();
    List<ComparisonResult> results = comparison.compare(ctx);
    assertEquals(1, results.size());
    assertEquals(Type.NODE_DIFF, results.get(0).getType());
    assertEquals("/rckmsOutput[1]/responseCode[1]/text()[1]", results.get(0).getNode());
  }

  private ComparisonContext createContext(String controlUri, String variantUri) throws Exception
  {
    String controlXml = XmlUtils.loadXml(controlUri);
    String variantXml = XmlUtils.loadXml(variantUri);
    ComparisonContext ctx =
        new ComparisonContext(null, null,
            createServiceOutput("control", controlXml), createServiceOutput("variant", variantXml));
    return ctx;
  }

  private ServiceOutput createServiceOutput(String sourceId, String xml)
  {
    ServiceOutput output = new ServiceOutput("test", "comparison-set-key", sourceId, 200, xml);
    return output;
  }

}

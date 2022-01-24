package org.cdsframework.rckms.compare.xmlunit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.cdsframework.rckms.SSComparisonServiceApplicationConfig.EnvType;
import org.cdsframework.rckms.compare.ComparisonContext;
import org.cdsframework.rckms.compare.ComparisonEngine;
import org.cdsframework.rckms.dao.ComparisonResult;
import org.cdsframework.rckms.dao.ComparisonResult.Type;
import org.cdsframework.rckms.dao.ServiceOutput;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlUnitComparisonEngine202103Test
{
  private static final Logger logger = LoggerFactory.getLogger(XmlUnitComparisonEngine202103Test.class);

  @Test
  public void testIgnoreKnownDifferences() throws Exception
  {
    ComparisonContext ctx = createContext("classpath:202103_control.xml", "classpath:202103_variant.xml");
    XmlUnitComparisonEngine202103 comparison = new XmlUnitComparisonEngine202103(EnvType.NONPROD);
    List<ComparisonResult> results = compare(comparison, ctx);
    assertTrue(results.isEmpty(), results.toString());
  }

  @Test
  public void testIgnoreKnownDifferences2() throws Exception
  {
    ComparisonContext ctx = createContext("classpath:202103_training_TC-Per_DD.xml", "classpath:202103_demo_TC-Per_DD.xml");
    XmlUnitComparisonEngine202103 comparison = new XmlUnitComparisonEngine202103(EnvType.NONPROD);
    List<ComparisonResult> results = compare(comparison, ctx);
    assertTrue(results.isEmpty(), results.toString());
  }

  @Test
  public void testUnexpectedDifference() throws Exception
  {
    // This variant contains a different responseCode
    ComparisonContext ctx = createContext("classpath:202103_control.xml", "classpath:202103_variant_with_diff.xml");
    XmlUnitComparisonEngine202103 comparison = new XmlUnitComparisonEngine202103(EnvType.NONPROD);
    List<ComparisonResult> results = compare(comparison, ctx);
    assertEquals(1, results.size());
    assertEquals(Type.NODE_DIFF, results.get(0).getType());
    assertEquals("/rckmsOutput[1]/jurisdiction[1]/serviceResponseCode[1]/text()[1]", results.get(0).getNode());
  }

  @Test
  public void testIgnoreExtraRSNFJurisdictionElements() throws Exception
  {
    ComparisonContext ctx = createContext("classpath:202103_RSNF_control.xml", "classpath:202103_RSNF_variant.xml");
    XmlUnitComparisonEngine202103 comparison = new XmlUnitComparisonEngine202103(EnvType.NONPROD);
    List<ComparisonResult> results = compare(comparison, ctx);
    assertTrue(results.isEmpty(), results.toString());
  }

  @Test
  public void testUnexpectedExtraJurisdictionElement() throws Exception
  {
    ComparisonContext ctx =
        createContext("classpath:202103_TestExtraJurisdiction_control.xml", "classpath:202103_TestExtraJurisdiction_variant.xml");
    XmlUnitComparisonEngine202103 comparison = new XmlUnitComparisonEngine202103(EnvType.NONPROD);
    List<ComparisonResult> results = compare(comparison, ctx);
    assertEquals("/rckmsOutput[1]/jurisdiction[1]", results.get(0).getNode());
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
    ServiceOutput output = new ServiceOutput("test", "comparison-set-key", sourceId);
    output.setServiceStatus(200);
    output.setOutput(xml);
    return output;
  }

  private List<ComparisonResult> compare(ComparisonEngine engine, ComparisonContext ctx)
  {
    List<ComparisonResult> results = engine.compare(ctx);
    int i = 0;
    for (ComparisonResult result : results)
    {
      logger.info("Diff[" + ++i + "]: " + result.toString() + ": " + result.getDescription());
    }
    return results;
  }

}

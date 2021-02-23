package org.cdsframework.rckms.compare.xmlunit;

import java.util.ArrayList;
import java.util.List;

import org.cdsframework.rckms.compare.ComparisonContext;
import org.cdsframework.rckms.compare.ComparisonEngine;
import org.cdsframework.rckms.dao.ComparisonResult;
import org.cdsframework.rckms.dao.ComparisonResult.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

abstract class AbstractXmlUnitComparisonEngine implements ComparisonEngine
{
  private static final Logger logger = LoggerFactory.getLogger(AbstractXmlUnitComparisonEngine.class);

  protected abstract DiffBuilder createDiffBuilder(String controlXml, String variantXml);

  @Override
  public List<ComparisonResult> compare(ComparisonContext context)
  {
    Diff diff = createDiffBuilder(context.getControlXml(), context.getVariantXml()).build();

    List<ComparisonResult> results = new ArrayList<>();
    diff.getDifferences().forEach(d -> results.add(transform(d, context)));
    //    int i = 0;
    //    for (Difference d : diff.getDifferences())
    //    {
    //      i++;
    //      Node controlNode = d.getComparison().getControlDetails().getTarget();
    //      Node testNode = d.getComparison().getTestDetails().getTarget();
    //      logger.info("Diff[" + i + "]: " + d.getComparison().getType() + "=" + d.getResult());
    //      logger.info(
    //          "Diff[" + i + "]: Control Node: " +
    //              (controlNode != null ? controlNode.getNodeName() : null) + ": " +
    //              (controlNode != null ? controlNode.getLocalName() : null) + ": "
    //              + controlNode);
    //      logger.info("Diff[" + i + "]: Control XPath: " + d.getComparison().getControlDetails().getXPath());
    //      logger.info("Diff[" + i + "]: Variant Node: " + (testNode != null ? testNode.getNodeName() : null) + ": " + testNode);
    //      logger.info("Diff[" + i + "]: Variant XPath: " + d.getComparison().getTestDetails().getXPath());
    //    }
    return results;
  }

  private ComparisonResult transform(Difference diff, ComparisonContext context)
  {
    ComparisonResult result;

    String nodePath = diff.getComparison().getControlDetails().getXPath() != null ?
                      diff.getComparison().getControlDetails().getXPath() : diff.getComparison().getTestDetails().getXPath();
    switch (diff.getComparison().getType())
    {
      case CHILD_LOOKUP:
      case ATTR_NAME_LOOKUP:
      case CHILD_NODELIST_LENGTH:
      case ELEMENT_NUM_ATTRIBUTES:
        result = new ComparisonResult(Type.NODE_UNMATCHED);
        break;
      case TEXT_VALUE:
      case ATTR_VALUE:
        result = new ComparisonResult(Type.NODE_DIFF);
        break;
      default:
        result = new ComparisonResult(Type.OTHER);
    }
    result.setNode(nodePath);
    result.setDescription(diff.toString());
    result.setControlServiceOutputId(context.getControl().getId());
    result.setVariantServiceOutputId(context.getVariant().getId());

    return result;
  }

}

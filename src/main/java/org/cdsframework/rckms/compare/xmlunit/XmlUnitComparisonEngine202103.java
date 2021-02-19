package org.cdsframework.rckms.compare.xmlunit;

import static org.cdsframework.rckms.compare.xmlunit.PredicateSupport.*;

import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.diff.ElementSelectors;

@Component
public class XmlUnitComparisonEngine202103 extends AbstractXmlUnitComparisonEngine
{
  @Override
  public String getId()
  {
    return "202103";
  }

  @Override
  protected DiffBuilder createDiffBuilder(String controlXml, String testXml)
  {
    return
        DiffBuilder.compare(Input.fromString(controlXml))
            .withTest(Input.fromString(testXml))
            .checkForSimilar()
            .ignoreWhitespace()
            .ignoreComments()
            // Note that NodeFilters are applied BEFORE comparisons (and before NodeMatcher), which makes sense.
            .withNodeFilter(nodeFilter())
            .withNodeMatcher(new DefaultNodeMatcher(elementMatchingStrategy()))
            .withDifferenceEvaluator(diffEvaluator());
  }

  private static ElementSelector elementMatchingStrategy()
  {
    return ElementSelectors.conditionalBuilder()
        .whenElementIsNamed("routingEntity")
        .thenUse(ElementSelectors.byNameAndAttributes("id"))
        .elseUse(ElementSelectors.byName)
        .build();
  }

  private static PredicateSupport<Node> nodeFilter()
  {
    return not(new NodeNamePredicate("diagnostics").withAncestry("rckmsOutput"))
        .and(not(new NodeNamePredicate("#text").withAncestry("input", "rckmsOutput")))
        .and(not(new NodeNamePredicate("#text").withAncestry("output", "jurisdiction", "rckmsOutput")));
  }

  private static DifferenceEvaluator diffEvaluator()
  {
    return DifferenceEvaluators.chain(DifferenceEvaluators.Default, ignoringExtraRoutingEntities());
  }

  private static DifferenceEvaluator ignoringExtraRoutingEntities()
  {
    // Note that this blindly treats ANY child length discrepancy for jurisdiction as OK, not just if the routingEntity
    // child occurrence is different. So the jurisdiction could have other unrelated elements that are triggering
    // the CHILD_NODELIST_LENGTH error that will be erroneously ignored here. However, we'll rely on those still being reported
    // by CHILD_LOOKUP errors later.
    // Unfortunately, there isn't really a way to know that the CHILD_NODELIST_LENGTH failure was due to a specific
    // child node.
    return DifferenceEvaluators.chain(new IgnoreComparisonTypeDifference(ComparisonType.CHILD_NODELIST_LENGTH)
            .onControlNode(new NodeNamePredicate("jurisdiction").withAncestry("rckmsOutput")),
        new IgnoreComparisonTypeDifference(ComparisonType.CHILD_LOOKUP)
            .onTestNode(new NodeNamePredicate("routingEntity").withAncestry("jurisdiction")));
  }
}

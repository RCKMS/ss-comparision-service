package org.cdsframework.rckms.compare.xmlunit;

import static org.cdsframework.rckms.compare.xmlunit.PredicateSupport.*;

import java.util.Collections;
import java.util.Map;

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
  private static final String RCKMS_NS_PREFIX = "rckms";
  private static final String RCKMS_NS = "org.cdsframework.rckms.output";
  private static final Map<String, String> namespaces = Collections.singletonMap(RCKMS_NS_PREFIX, RCKMS_NS);

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
            .withNodeMatcher(new DefaultNodeMatcher(elementPairingStrategy()))
            .withDifferenceEvaluator(diffEvaluator());
  }

  private static ElementSelector elementPairingStrategy()
  {
    return ElementSelectors.conditionalBuilder()
        // This pairs up routingEntity elements between control and variant based on the combination of the element name
        // (routingEntity) and its id attribute. This ensures that at a minimum, the single routingEntity we expect in the control
        // doc is present in the variant.
        .whenElementIsNamed("routingEntity")
        .thenUse(ElementSelectors.byNameAndAttributes("id"))

        // To handle the rename of serviceResponseCode->responseCode, this pairs the serviceResponseCode element in the control doc
        // to the responseCode element in the variant doc
        .when(new NodeNamePredicate("jurisdiction/serviceResponseCode"))
        .thenUse((control, variant) -> variant.getTagName().equals("responseCode"))

        // To handle the rename of serviceResponseMessage->responseMessage, this pairs the serviceResponseMessage element in the
        // control doc to the responseMessage element in the variant doc
        .when(new NodeNamePredicate("jurisdiction/serviceResponseMessage"))
        .thenUse((control, variant) -> variant.getTagName().equals("responseMessage"))

        // Pair up reportingCondition elements based on equivalent conditionCode values
        .whenElementIsNamed("reportingCondition")
        .thenUse(pairByChildElementText("conditionCode"))

        // Pair up linkAndReference elements based on equivalent id values
        .whenElementIsNamed("linkAndReference")
        .thenUse(pairByChildElementText("id"))

        // Pair up logicSet elements based on equivalent id values
        .whenElementIsNamed("logicSet")
        .thenUse(pairByChildElementText("id"))

        // Pair up criteria elements based on equivalent criteriaId values
        .whenElementIsNamed("criteria")
        .thenUse(pairByChildElementText("criteriaId"))

        // Pair up responsibleAgency elements based on equivalent id values
        .whenElementIsNamed("responsibleAgency")
        .thenUse(ElementSelectors.byNameAndAttributes("id"))

        // Otherwise, by default, just pair up elements by their name and text value
        .elseUse(ElementSelectors.byNameAndText)
        .build();
  }

  private static ElementSelector pairByChildElementText(String childName)
  {
    // The XPath arg specifies which child element to look at. The ElementSelector arg specifies how they should be matched.
    // So this is saying to match when the specified childName has the same name and text content
    return ElementSelectors.byXPath("./" + RCKMS_NS_PREFIX + ":" + childName, namespaces, ElementSelectors.byNameAndText);
  }

  private static PredicateSupport<Node> nodeFilter()
  {
    return not(new NodeNamePredicate("rckmsOutput/diagnostics"))
        .and(not(new NodeNamePredicate("rckmsOutput/input/#text")))
        // Output is never the same, I believe because it encodes a timestamp
        .and(not(new NodeNamePredicate("rckmsOutput/output/#text")))
        // serviceMessage text (and the number of them) may have changed and can be ignored altogther
        .and(not(new NodeNamePredicate("rckmsOutput/serviceMessage")));
  }

  private static DifferenceEvaluator diffEvaluator()
  {
    return DifferenceEvaluators.chain(
        DifferenceEvaluators.Default,
        ignoringExtraRoutingEntities(),
        ignoringLocationRelevance(),
        ignoringNewResponseMessageInVariant(),
        // Even though the elementPairingStrategy() will pair serviceResponseCode to responseCode, we still have to
        // explicitly tell XmlUnit to ignore the name change.
        ignoringElementNameChange("jurisdiction/responseCode"),
        ignoringElementNameChange("jurisdiction/responseMessage"));
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
            .onControlNode(new NodeNamePredicate("rckmsOutput/jurisdiction")),
        new IgnoreComparisonTypeDifference(ComparisonType.CHILD_LOOKUP)
            .onTestNode(new NodeNamePredicate("jurisdiction/routingEntity")));
  }

  private static DifferenceEvaluator ignoringLocationRelevance()
  {
    return DifferenceEvaluators.chain(
        // Ignore that the count of jurisdiction child nodes will be different
        new IgnoreComparisonTypeDifference(ComparisonType.CHILD_NODELIST_LENGTH)
            .onControlNode(new NodeNamePredicate("rckmsOutput/jurisdiction")),
        // Ignore that count of reportingCondition child nodes will be different
        new IgnoreComparisonTypeDifference(ComparisonType.CHILD_NODELIST_LENGTH)
            .onControlNode(new NodeNamePredicate("jurisdiction/reportingCondition")),
        // Ignore that the jurisdiction/locationRelevance in the control doc won't be present in the variant doc
        new IgnoreComparisonTypeDifference(ComparisonType.CHILD_LOOKUP)
            .onControlNode(new NodeNamePredicate("jurisdiction/locationRelevance")),
        // Ignore that the jurisdiction/reportingCondition/locationRelevance in the variant doc won't be present in the control doc
        new IgnoreComparisonTypeDifference(ComparisonType.CHILD_LOOKUP)
            .onTestNode(new NodeNamePredicate("reportingCondition/locationRelevance")));
  }

  private static DifferenceEvaluator ignoringNewResponseMessageInVariant()
  {
    return DifferenceEvaluators.chain(
        // Ignore that the count of rckmsOutput child nodes will be different because the new version contains an extra
        // top-level responseMessage node
        new IgnoreComparisonTypeDifference(ComparisonType.CHILD_NODELIST_LENGTH)
            .onControlNode(new NodeNamePredicate("rckmsOutput")),
        // Ignore that the responseMessage in the variant doc won't be present in the control doc
        new IgnoreComparisonTypeDifference(ComparisonType.CHILD_LOOKUP)
            .onTestNode(new NodeNamePredicate("rckmsOutput/responseMessage")));
  }

  private static DifferenceEvaluator ignoringElementNameChange(String newElement)
  {
    return
        new IgnoreComparisonTypeDifference(ComparisonType.ELEMENT_TAG_NAME)
            .onTestNode(new NodeNamePredicate(newElement));
  }

}

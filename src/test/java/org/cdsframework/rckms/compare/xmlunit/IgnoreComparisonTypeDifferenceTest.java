package org.cdsframework.rckms.compare.xmlunit;

import static org.springframework.test.util.AssertionErrors.*;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.util.Predicate;

public class IgnoreComparisonTypeDifferenceTest
{
  @Test
  public void testExtraChildNode()
  {
    String control = "<a><b></b></a>";
    String test = "<a><b></b><c></c></a>";

    Predicate<Node> parentNode = new NodeNamePredicate("a");
    Predicate<Node> extraChildNode = new NodeNamePredicate("c");
    DifferenceEvaluator ignoreExtraChildrenInTestDoc = DifferenceEvaluators.chain(
        new IgnoreComparisonTypeDifference(ComparisonType.CHILD_NODELIST_LENGTH).onControlNode(parentNode),
        new IgnoreComparisonTypeDifference(ComparisonType.CHILD_LOOKUP).onTestNode(extraChildNode)
    );
    Diff myDiff = DiffBuilder.compare(Input.fromString(control))
        .withTest(Input.fromString(test))
        .withDifferenceEvaluator(DifferenceEvaluators.chain(DifferenceEvaluators.Default, ignoreExtraChildrenInTestDoc))
        .checkForSimilar()
        .build();

    assertFalse(myDiff.toString(), myDiff.hasDifferences());
  }

  @Test
  public void testExtraRepeatedChildNode()
  {
    String control = "<a><b></b></a>";
    String test = "<a><b></b><b></b></a>";

    Predicate<Node> parentNode = new NodeNamePredicate("a");
    Predicate<Node> extraChildNode = new NodeNamePredicate("b");
    DifferenceEvaluator ignoreExtraChildrenInTestDoc = DifferenceEvaluators.chain(
        new IgnoreComparisonTypeDifference(ComparisonType.CHILD_NODELIST_LENGTH).onControlNode(parentNode),
        new IgnoreComparisonTypeDifference(ComparisonType.CHILD_LOOKUP).onTestNode(extraChildNode)
    );
    Diff myDiff = DiffBuilder.compare(Input.fromString(control))
        .withTest(Input.fromString(test))
        .withDifferenceEvaluator(DifferenceEvaluators.chain(DifferenceEvaluators.Default, ignoreExtraChildrenInTestDoc))
        .checkForSimilar()
        .build();
    assertFalse(myDiff.toString(), myDiff.hasDifferences());

    // Make sure we catch other elements (<c>) not explicitly ignored
    test = "<a><b></b><b></b><c/></a>";
    myDiff = DiffBuilder.compare(Input.fromString(control))
        .withTest(Input.fromString(test))
        .withDifferenceEvaluator(DifferenceEvaluators.chain(DifferenceEvaluators.Default, ignoreExtraChildrenInTestDoc))
        .checkForSimilar()
        .build();
    assertTrue(myDiff.toString(), myDiff.hasDifferences());
  }
}

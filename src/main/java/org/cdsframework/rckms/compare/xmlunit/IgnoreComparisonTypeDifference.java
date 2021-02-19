package org.cdsframework.rckms.compare.xmlunit;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.util.Predicate;

final class IgnoreComparisonTypeDifference implements DifferenceEvaluator
{
  private static final Logger logger = LoggerFactory.getLogger(IgnoreComparisonTypeDifference.class);

  private final ComparisonType comparisonType;
  private Predicate<Node> nodePredicate;
  private Function<org.xmlunit.diff.Comparison, Node> target;

  public IgnoreComparisonTypeDifference(ComparisonType comparisonType)
  {
    this.comparisonType = comparisonType;
  }

  public IgnoreComparisonTypeDifference onControlNode(Predicate<Node> nodePredicate)
  {
    this.nodePredicate = nodePredicate;
    target = (comparison) -> comparison.getControlDetails().getTarget();
    return this;
  }

  public IgnoreComparisonTypeDifference onTestNode(Predicate<Node> nodePredicate)
  {
    this.nodePredicate = nodePredicate;
    target = (comparison) -> comparison.getTestDetails().getTarget();
    return this;
  }

  @Override
  public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome)
  {
    // Pass through comparisons we are not interested in
    if (!comparisonType.equals(comparison.getType()))
      return outcome;

    Node targetNode = target.apply(comparison);
    if (targetNode == null)
      return outcome;

    if (nodePredicate.test(targetNode))
    {
      //      if (logger.isDebugEnabled())
      //        logger.debug("Ignoring difference[{}] on nodes: Control={}; Test={}", comparisonType,
      //            comparison.getControlDetails().getXPath(), comparison.getTestDetails().getXPath());
      return ComparisonResult.SIMILAR;
    }

    return outcome;
  }
}

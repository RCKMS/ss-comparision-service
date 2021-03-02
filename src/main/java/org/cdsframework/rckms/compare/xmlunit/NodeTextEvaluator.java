package org.cdsframework.rckms.compare.xmlunit;

import java.util.Objects;
import java.util.function.BiPredicate;

import org.w3c.dom.Node;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.util.Predicate;

/**
 * Allows customization of how node text values (e.g. element text or attribute values) should be compared. This allows you to
 * do things like creating case-insensitive comparisons or transformations of the values before comparing.
 */
public class NodeTextEvaluator implements DifferenceEvaluator
{
  private static final BiPredicate<String, String> CASE_INSENSITIVE_EQUALITY = (s1, s2) -> s1.equalsIgnoreCase(s2);
  private final BiPredicate<String, String> evaluator;
  private Predicate<Node> nodePredicate;

  /**
   * Constructs a NodeTextEvaluator that delegates comparison of the control and variant values based on the given
   * BiPredicate. The evaluator will be invoked using the control and variant values respectively and they are
   * guaranteed to be non-null.
   *
   * @param evaluator
   */
  public NodeTextEvaluator(BiPredicate<String, String> evaluator)
  {
    this.evaluator = evaluator;
  }

  /**
   * Convenience builder method for an evaluator that ignores case differences between control and variant
   *
   * @return
   */
  public static NodeTextEvaluator ignoreCase()
  {
    return new NodeTextEvaluator(CASE_INSENSITIVE_EQUALITY);
  }

  public NodeTextEvaluator onNode(NodeNamePredicate nodeMatcher)
  {
    this.nodePredicate = nodeMatcher;
    return this;
  }

  @Override
  public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome)
  {
    Objects.requireNonNull(nodePredicate,
        "No NodeNamePredicate has been specified. Make sure you've called onNode(NodeNamePredicate)");

    // Pass through comparisons we are not interested in
    if (!applies(comparison))
      return outcome;

    // Delegate to the evaluator to determine if the test matches
    if (evaluator.test((String) comparison.getControlDetails().getValue(), (String) comparison.getTestDetails().getValue()))
      return ComparisonResult.SIMILAR;

    return outcome;
  }

  private boolean applies(Comparison comparison)
  {
    switch (comparison.getType())
    {
      case TEXT_VALUE:
      case ATTR_VALUE:
        return nodePredicate.test(comparison.getControlDetails().getTarget());
      default:
        return false;
    }
  }
}

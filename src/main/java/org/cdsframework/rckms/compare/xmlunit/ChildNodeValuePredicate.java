package org.cdsframework.rckms.compare.xmlunit;

import java.util.Objects;

import org.w3c.dom.Node;
import org.xmlunit.util.Predicate;

/**
 * A Node Predicate that will return true if a node has a specified child node whose text content matches a given value.
 *
 * @param <T>
 */
final class ChildNodeValuePredicate<T extends Node> implements PredicateSupport<T>
{
  private final Predicate<T> parentSelector;
  private String childName;
  private String childValue;
  private boolean isChildAnAttribute = false;

  private ChildNodeValuePredicate(Predicate<T> parentSelector)
  {
    this.parentSelector = parentSelector;
  }

  public static <T extends Node> ChildNodeValuePredicate<T> forParent(Predicate<T> parentSelector)
  {
    return new ChildNodeValuePredicate<>(parentSelector);
  }

  public static <T extends Node> ChildNodeValuePredicate<T> forParent(String path)
  {
    return new ChildNodeValuePredicate<>(NodeNamePredicate.pathMatching(path));
  }

  public ChildNodeValuePredicate<T> withChildElement(String childName, String childValue)
  {
    this.childName = childName;
    this.childValue = childValue;
    return this;
  }

  public ChildNodeValuePredicate<T> withChildAttribute(String childName, String childValue)
  {
    this.childName = childName;
    this.childValue = childValue;
    this.isChildAnAttribute = true;
    return this;
  }

  @Override
  public boolean test(T node)
  {
    return parentSelector.test(node) && childMatches(node);
  }

  private boolean childMatches(Node node)
  {
    if (isChildAnAttribute)
    {
      Node childNode = node.getAttributes().getNamedItem(childName);
      return childNode != null && Objects.equals(childValue, childNode.getTextContent());
    }
    else
    {
      for (int i = 0; i < node.getChildNodes().getLength(); i++)
      {
        Node childNode = node.getChildNodes().item(i);
        if (Objects.equals(childName, childNode.getNodeName()) && Objects.equals(childValue, childNode.getTextContent()))
          return true;
      }
    }
    return false;
  }

}

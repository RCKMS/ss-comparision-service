package org.cdsframework.rckms.compare.xmlunit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * XmlUnit Predicate to match nodes for things like node/attribute filters and pairing strategies.
 * This is mostly needed because most of XmlUnit's built-in mechanisms just allow you to identify a node by name alone without
 * any path context so you could easily have a predicate that applies to the wrong node simply because it uses the same name.
 * This implementation allows you to specify at least a partial path to match against.
 *
 * @param <T>
 */
final class NodeNamePredicate<T extends Node> implements PredicateSupport<T>
{
  private final String name;
  private String[] ancestors;

  /**
   * Creates a predicate matching the specified name or path.
   * If the param contains one or more "/" characters, then it is interpreted similarly to an XPath expression
   * where the "/" specify the ancestry, starting with the oldest and ending with the target node.
   * However, note that this is *NOT* true XPath, it's just simplified way of expressing a node path
   * <p>
   * For example, given the xml <code><root><child1><child2/></child1></root></code>, if you wanted to target the child2 node
   * then you can do
   * <code>
   * NodeNamePredicate.forPathFragment("root/child1/child2")
   * </code>
   * <p>
   * Partial ancestries are also supported, like this:
   * <code>
   * NodeNamePredicate.forPathFragment("child1/child2")
   * </code>
   *
   * @param path
   */
  public static <T extends Node> NodeNamePredicate<T> pathMatching(String path)
  {
    return new NodeNamePredicate<>(path);
  }

  private NodeNamePredicate(String path)
  {
    if (!path.contains("/"))
      this.name = path;
    else
    {
      List<String> ancestry = Arrays.asList(path.split("/"));
      Collections.reverse(ancestry);
      this.name = ancestry.get(0);
      withAncestry(ancestry.subList(1, ancestry.size()));
    }
  }

  /**
   * Can be a partial or complete ancestry, in order from closest (youngest) to furthest (oldest)
   *
   * @param ancestor
   * @return
   */
  NodeNamePredicate<T> withAncestry(String... ancestor)
  {
    ancestors = ancestor;
    return this;
  }

  /**
   * Can be a partial or complete ancestry, in order from closest (youngest) to furthest (oldest)
   *
   * @param ancestors
   * @return
   */
  NodeNamePredicate<T> withAncestry(List<String> ancestors)
  {
    withAncestry(ancestors.toArray(new String[ancestors.size()]));
    return this;
  }

  @Override
  public boolean test(T node)
  {
    return name.equals(node.getNodeName()) && ancestryMatches(node);
  }

  private boolean ancestryMatches(Node node)
  {
    if (ancestors == null)
      return true;
    Node currentNode = node;
    for (int i = 0; i < ancestors.length; i++)
    {
      if (currentNode == null || !parentMatches(currentNode, ancestors[i]))
        return false;
      currentNode = currentNode.getParentNode();
    }
    return true;
  }

  private boolean parentMatches(Node node, String parentName)
  {
    Node parent = node.getParentNode();
    switch (node.getNodeType())
    {
      case Node.ATTRIBUTE_NODE:
        return parentName.equals(((Attr) node).getOwnerElement().getNodeName());
      default:
        return node.getParentNode() != null && parentName.equals(node.getParentNode().getNodeName());
    }
  }
}

package org.cdsframework.rckms.compare;

import org.w3c.dom.Node;

final class NodeNamePredicate implements PredicateSupport<Node>
{
  private final String name;
  private String[] ancestors;

  public NodeNamePredicate(String name)
  {
    this.name = name;
  }

  NodeNamePredicate withAncestry(String... ancestor)
  {
    ancestors = ancestor;
    return this;
  }

  @Override
  public boolean test(Node node)
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
    return parent != null && parentName.equals(parent.getNodeName());
  }
}

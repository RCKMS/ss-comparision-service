package org.cdsframework.rckms.compare.xmlunit;

import org.w3c.dom.Attr;
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
    switch (node.getNodeType())
    {
      case Node.ATTRIBUTE_NODE:
        return parentName.equals(((Attr) node).getOwnerElement().getNodeName());
      default:
        return node.getParentNode() != null && parentName.equals(node.getParentNode().getNodeName());
    }
  }
}

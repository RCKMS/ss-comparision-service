package org.cdsframework.rckms.dao;

import java.util.Objects;

public class ComparisonResult
{

  public enum Type
  {
    CONTROL_MISSING(),
    VARIANT_MISSING(),
    STATUS_DIFF(),
    NODE_UNMATCHED(),
    NODE_DIFF(),
    OTHER()
  }

  private String node;
  private String description;
  private Type type;

  ComparisonResult()
  {
  }

  public ComparisonResult(Type type)
  {
    this.type = type;
  }

  public String getNode()
  {
    return node;
  }

  public void setNode(String node)
  {
    this.node = node;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public Type getType()
  {
    return type;
  }

  public void setType(Type type)
  {
    this.type = type;
  }

  @Override
  public String toString()
  {
    return node;
  }

  @Override
  public boolean equals(Object o)
  {
    if (!(o instanceof ComparisonResult))
      return false;
    if (this == o)
      return true;
    ComparisonResult other = (ComparisonResult) o;
    return Objects.equals(this.getNode(), other.getNode());
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.getNode());
  }
}

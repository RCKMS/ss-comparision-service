package org.cdsframework.rckms.dao;

import java.util.Objects;

public class ComparisonResult
{

  public enum Status
  {
    UNMATCHED(),
    DIFF()
  }

  private String node;
  private String description;
  private Status status;

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

  public Status getStatus()
  {
    return status;
  }

  public void setStatus(Status status)
  {
    this.status = status;
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

package org.cdsframework.rckms.dao;

import java.time.OffsetDateTime;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "service_output")
@CompoundIndexes({
                     @CompoundIndex(name = "key_source", def = "{'comparisonSetKey' : 1, 'sourceId': 1}", unique = true)
                 })
public class ServiceOutput
{

  ServiceOutput()
  {
  }

  public ServiceOutput(String comparisonSetKey, String sourceId, int serviceStatus, String output)
  {
    this.comparisonSetKey = comparisonSetKey;
    this.sourceId = sourceId;
    this.serviceStatus = serviceStatus;
    this.output = output;
    this.createDate = OffsetDateTime.now();
  }

  @Id
  private String id;

  @Field
  private String comparisonSetKey;

  @Field
  private String sourceId;

  @Field
  private int serviceStatus;

  @Field
  private String output;

  @Field
  private OffsetDateTime createDate;

  @Field
  private OffsetDateTime comparisonDate;

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getComparisonSetKey()
  {
    return comparisonSetKey;
  }

  public void setComparisonSetKey(String comparisonSetKey)
  {
    this.comparisonSetKey = comparisonSetKey;
  }

  public String getSourceId()
  {
    return sourceId;
  }

  public void setSourceId(String sourceId)
  {
    this.sourceId = sourceId;
  }

  public int getServiceStatus()
  {
    return serviceStatus;
  }

  public void setServiceStatus(int serviceStatus)
  {
    this.serviceStatus = serviceStatus;
  }

  public String getOutput()
  {
    return output;
  }

  public void setOutput(String output)
  {
    this.output = output;
  }

  public OffsetDateTime getCreateDate()
  {
    return createDate;
  }

  public void setCreateDate(OffsetDateTime createDate)
  {
    this.createDate = createDate;
  }

  public OffsetDateTime getComparisonDate()
  {
    return comparisonDate;
  }

  public void setComparisonDate(OffsetDateTime comparisonDate)
  {
    this.comparisonDate = comparisonDate;
  }

  @Override
  public String toString()
  {
    return comparisonSetKey + "[" + sourceId + "]";
  }

  @Override
  public boolean equals(Object o)
  {
    if (!(o instanceof ServiceOutput))
      return false;
    if (this == o)
      return true;
    ServiceOutput other = (ServiceOutput) o;
    return Objects.equals(this.getComparisonSetKey(), other.getComparisonSetKey())
        && Objects.equals(this.getSourceId(), other.getSourceId());
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.getComparisonSetKey(), this.getSourceId());
  }
}

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
                     // This index can't be unique because it's possible that AIMS receives an retryable error,
                     // so we'd get more than one output file for the same eICR instance
                     @CompoundIndex(name = "key_source", def = "{'comparisonSetKey' : 1, 'sourceId': 1}", unique = false)
                 })
public class ServiceOutput
{

  ServiceOutput()
  {
  }

  public ServiceOutput(String comparisonTestId, String comparisonSetKey, String sourceId, int serviceStatus, String output)
  {
    this.comparisonTestId = comparisonTestId;
    this.comparisonSetKey = comparisonSetKey;
    this.sourceId = sourceId;
    this.serviceStatus = serviceStatus;
    this.output = output;
    this.createDate = OffsetDateTime.now();
  }

  @Id
  private String id;

  @Field
  @Indexed
  private String comparisonTestId;

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
  // See MongoConfig for the TTL index on this.
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

  public String getComparisonTestId()
  {
    return comparisonTestId;
  }

  public void setComparisonTestId(String comparisonTestId)
  {
    this.comparisonTestId = comparisonTestId;
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
    return Objects.equals(this.getId(), other.getId());
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.getId());
  }
}

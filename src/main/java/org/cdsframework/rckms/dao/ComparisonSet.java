package org.cdsframework.rckms.dao;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Document(collection = "comparison_set")
@CompoundIndexes({
                     @CompoundIndex(name = "comparisonTestId_createDate", def = "{'comparisonTestId' : 1, 'createDate': 1}")
                 })

public class ComparisonSet
{

  public enum Status
  {
    // haven't received a complete pair of outputs yet
    INCOMPLETE(),
    // Received at least 2 outputs and awaiting processing
    PENDING(),
    // Comparison completed and passed
    PASS(),
    // Comparison completed with failures/differences
    FAIL()
  }

  @Id
  private String id;

  @Field
  private String comparisonTestId;

  @Field
  @Indexed(unique = true)
  private String comparisonSetKey;

  @Field
  private int serviceOutputCount;

  @Field
  @Indexed
  private OffsetDateTime createDate;

  @Field
  private OffsetDateTime comparisonDate;

  @Field
  private Status status;

  @Field
  private List<ComparisonResult> results;

  // Note that the ServiceOutput is only attached directly here after the comparison has been evaluated.
  // AND note that we do NOT include the ServiceOutput.output (xml payload) when this ComparisonSet is in a
  // PASS status. If FAIL, then it will contain the full output
  @Field
  private List<ServiceOutput> serviceOutputs;

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

  public List<ComparisonResult> getResults()
  {
    return results;
  }

  public void setResults(List<ComparisonResult> results)
  {
    this.results = results;
  }

  public int getServiceOutputCount()
  {
    return serviceOutputCount;
  }

  public void setServiceOutputCount(int serviceOutputCount)
  {
    this.serviceOutputCount = serviceOutputCount;
  }

  public String getComparisonTestId()
  {
    return comparisonTestId;
  }

  public void setComparisonTestId(String comparisonTestId)
  {
    this.comparisonTestId = comparisonTestId;
  }

  public Status getStatus()
  {
    return status;
  }

  public void setStatus(Status status)
  {
    this.status = status;
  }

  // This is excluded from json output because it is only set in its complete state on FAIL instances and, for consistency, retrieval of the output
  // regardless of status should be driven through the /output endpoint
  @JsonIgnore
  public List<ServiceOutput> getServiceOutputs()
  {
    return serviceOutputs;
  }

  public void setServiceOutputs(List<ServiceOutput> serviceOutputs)
  {
    this.serviceOutputs = serviceOutputs;
  }

  public Map<String, Double> getServiceResponseTimes()
  {
    if (serviceOutputs == null)
      return null;
    Map<String, Double> averages = serviceOutputs.stream()
        .filter(o -> o.getServiceResponseTime() != null)
        .collect(Collectors.groupingBy(o -> o.getSourceId(), Collectors.averagingDouble(ServiceOutput::getServiceResponseTime)));
    return averages;
  }

  @Override
  public boolean equals(Object o)
  {
    if (!(o instanceof ComparisonSet))
      return false;
    if (this == o)
      return true;
    ComparisonSet other = (ComparisonSet) o;
    return Objects.equals(this.getComparisonSetKey(), other.getComparisonSetKey());
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.getComparisonSetKey());
  }

  @Override
  public String toString()
  {
    return comparisonSetKey;
  }
}

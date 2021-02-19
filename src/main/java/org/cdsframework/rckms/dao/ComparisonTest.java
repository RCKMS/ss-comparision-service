package org.cdsframework.rckms.dao;

import java.time.OffsetDateTime;
import java.util.Objects;

import javax.validation.constraints.NotBlank;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "comparison_test")
public class ComparisonTest
{

  @Id
  @NotBlank
  private String id;

  @Field
  private String description;

  @Field
  @NotBlank
  private String controlSourceId;

  @Field
  @NotBlank
  private String comparisonEngineId = "default";

  @Field
  @Indexed
  private OffsetDateTime createDate;

  ComparisonTest()
  {
  }

  public ComparisonTest(@NotBlank String id, @NotBlank String controlSourceId)
  {
    this.id = id;
    this.controlSourceId = controlSourceId;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getControlSourceId()
  {
    return controlSourceId;
  }

  public void setControlSourceId(String controlSourceId)
  {
    this.controlSourceId = controlSourceId;
  }

  public OffsetDateTime getCreateDate()
  {
    return createDate;
  }

  public void setCreateDate(OffsetDateTime createDate)
  {
    this.createDate = createDate;
  }

  public String getComparisonEngineId()
  {
    return comparisonEngineId;
  }

  public void setComparisonEngineId(String comparisonEngineId)
  {
    this.comparisonEngineId = comparisonEngineId;
  }

  @Override
  public boolean equals(Object o)
  {
    if (!(o instanceof ComparisonTest))
      return false;
    if (this == o)
      return true;
    ComparisonTest other = (ComparisonTest) o;
    return Objects.equals(this.getId(), other.getId());
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.getId());
  }

  @Override
  public String toString()
  {
    return id;
  }
}

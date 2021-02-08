package org.cdsframework.rckms.dao;

import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "comparison_queue")
@CompoundIndexes({
                     @CompoundIndex(name = "status_create_date", def = "{'status' : 1, 'createDate': 1}")
                 })
public class QueueRecord
{

  public enum QueueStatus
  {
    PENDING(),
    PROCESSING(),
    ERROR()
  }

  @Id
  private String id;

  @Field
  @Indexed(unique = true)
  private String comparisonSetKey;

  @Field
  private OffsetDateTime createDate;

  @Field
  private QueueStatus status = QueueStatus.PENDING;

  @Field
  private OffsetDateTime statusDate;

  @Field
  private String error;

  QueueRecord()
  {
  }

  public QueueRecord(String comparisonSetKey)
  {
    this.comparisonSetKey = comparisonSetKey;
    this.createDate = OffsetDateTime.now();
    this.status = QueueStatus.PENDING;
    this.statusDate = OffsetDateTime.now();
  }

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

  public QueueStatus getStatus()
  {
    return status;
  }

  public void setStatus(QueueStatus status)
  {
    this.status = status;
  }

  public OffsetDateTime getStatusDate()
  {
    return statusDate;
  }

  public void setStatusDate(OffsetDateTime statusDate)
  {
    this.statusDate = statusDate;
  }

  public String getError()
  {
    return error;
  }

  public void setError(String error)
  {
    this.error = error;
  }
}

package org.cdsframework.rckms.rest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.cdsframework.rckms.dao.ComparisonResult;
import org.cdsframework.rckms.dao.ComparisonResult.Type;
import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ComparisonSet.Status;
import org.cdsframework.rckms.dao.ServiceOutput;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(
    { "comparisonSetKey", "comparisonTestId", "status", "createDate", "comparisonDate", "serviceOutputCount", "serviceOutput",
        "results" })
public class ComparisonSetDetails
{
  private ComparisonSet comparisonSet;
  private List<ServiceOutput> outputs;

  public ComparisonSetDetails(ComparisonSet comparisonSet, List<ServiceOutput> outputs)
  {
    this.comparisonSet = comparisonSet;
    this.outputs = outputs;
  }

  public String getComparisonTestId()
  {
    return comparisonSet.getComparisonTestId();
  }

  public String getComparisonSetKey()
  {
    return comparisonSet.getComparisonSetKey();
  }

  public int getServiceOutputCount()
  {
    return comparisonSet.getServiceOutputCount();
  }

  public OffsetDateTime getCreateDate()
  {
    return comparisonSet.getCreateDate();
  }

  public OffsetDateTime getComparisonDate()
  {
    return comparisonSet.getComparisonDate();
  }

  public Status getStatus()
  {
    return comparisonSet.getStatus();
  }

  public List<ComparisonResultDTO> getResults()
  {
    return comparisonSet.getResults().stream()
        .map(r -> new ComparisonResultDTO(r, comparisonSet.getComparisonSetKey()))
        .collect(Collectors.toList());
  }

  public List<ServiceOutputDTO> getServiceOutput()
  {
    return outputs.stream()
        .map(ServiceOutputDTO::new)
        .collect(Collectors.toList());
  }

  public static final class ServiceOutputDTO
  {
    private final ServiceOutput serviceOutput;

    public ServiceOutputDTO(ServiceOutput serviceOutput)
    {
      this.serviceOutput = serviceOutput;
    }

    public String getId()
    {
      return serviceOutput.getId();
    }

    public String getSourceId()
    {
      return serviceOutput.getSourceId();
    }

    public int getServiceStatus()
    {
      return serviceOutput.getServiceStatus();
    }

    public String getOutputUrl()
    {
      return createOutputUrl(serviceOutput.getComparisonSetKey(), getId());
    }

    public OffsetDateTime getCreateDate()
    {
      return serviceOutput.getCreateDate();
    }

    static String createOutputUrl(String comparisonSetKey, String outputId)
    {
      return ServletUriComponentsBuilder.fromCurrentContextPath()
          .path("ss-comparison-service/v1/management/comparison-sets/{comp-set-key}/output/{output-id}/xml")
          .build(comparisonSetKey, outputId).toString();
    }
  }

  public static final class ComparisonResultDTO
  {
    private final ComparisonResult result;
    private final String comparisonSetKey;

    public ComparisonResultDTO(ComparisonResult result, String comparisonSetKey)
    {
      this.result = result;
      this.comparisonSetKey = comparisonSetKey;
    }

    public String getNode()
    {
      return result.getNode();
    }

    public String getDescription()
    {
      return result.getDescription();
    }

    public Type getType()
    {
      return result.getType();
    }

    public String getControlServiceOutputUrl()
    {
      return createUrl(result.getControlServiceOutputId());
    }

    public String getVariantServiceOutputUrl()
    {
      return createUrl(result.getVariantServiceOutputId());
    }

    private String createUrl(String outputId)
    {
      return ServiceOutputDTO.createOutputUrl(comparisonSetKey, outputId).toString();
    }
  }
}

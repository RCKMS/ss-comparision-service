package org.cdsframework.rckms.rest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.cdsframework.rckms.dao.ComparisonResult;
import org.cdsframework.rckms.dao.ComparisonResult.Type;
import org.cdsframework.rckms.dao.ComparisonSet;
import org.cdsframework.rckms.dao.ComparisonSet.Status;
import org.cdsframework.rckms.dao.ServiceOutput;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(
    { "comparisonSetKey", "comparisonTestId", "status", "createDate", "comparisonDate", "serviceOutputCount", "serviceOutput",
        "results" })
public class ComparisonSetDetails
{
  private String managementBaseUrl;
  private ComparisonSet comparisonSet;
  private List<ServiceOutput> outputs;

  public ComparisonSetDetails(ComparisonSet comparisonSet, List<ServiceOutput> outputs, String managementBaseUrl)
  {
    this.managementBaseUrl = managementBaseUrl;
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
        .map(r -> new ComparisonResultDTO(r, comparisonSet.getComparisonSetKey(), managementBaseUrl))
        .collect(Collectors.toList());
  }

  public List<ServiceOutputDTO> getServiceOutput()
  {
    return outputs.stream()
        .map((output) -> new ServiceOutputDTO(output, managementBaseUrl))
        .collect(Collectors.toList());
  }

  public static final class ServiceOutputDTO
  {
    private String managementBaseUrl;
    private final ServiceOutput serviceOutput;

    public ServiceOutputDTO(ServiceOutput serviceOutput, String managementBaseUrl)
    {
      this.managementBaseUrl = managementBaseUrl;
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
      return createOutputUrl(serviceOutput.getComparisonSetKey(), getId(), managementBaseUrl);
    }

    public OffsetDateTime getCreateDate()
    {
      return serviceOutput.getCreateDate();
    }

    static String createOutputUrl(String comparisonSetKey, String outputId, String managementBaseUrl)
    {
      if (managementBaseUrl != null)
      {
        return UriComponentsBuilder.fromUriString(managementBaseUrl)
            .path("/comparison-sets/{comp-set-key}/output/{output-id}/xml")
            .build(comparisonSetKey, outputId).toString();
      }
      else
      {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = ((ServletRequestAttributes) requestAttributes).getRequest();
        return UriComponentsBuilder.fromUriString(req.getRequestURL().toString())
            .replacePath("ss-comparison-service/v1/management/comparison-sets/{comp-set-key}/output/{output-id}/xml")
            .build(comparisonSetKey, outputId).toString();
      }
    }
  }

  public static final class ComparisonResultDTO
  {
    private final ComparisonResult result;
    private final String comparisonSetKey;
    private String managementBaseUrl;

    public ComparisonResultDTO(ComparisonResult result, String comparisonSetKey, String managementBaseUrl)
    {
      this.result = result;
      this.comparisonSetKey = comparisonSetKey;
      this.managementBaseUrl = managementBaseUrl;
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
      return ServiceOutputDTO.createOutputUrl(comparisonSetKey, outputId, managementBaseUrl).toString();
    }
  }
}

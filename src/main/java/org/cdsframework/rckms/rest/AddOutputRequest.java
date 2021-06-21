package org.cdsframework.rckms.rest;

import javax.validation.constraints.Positive;

public class AddOutputRequest
{
  @Positive
  private int serviceStatus;
  private String serviceOutput;
  private Integer serviceResponseTime;

  public int getServiceStatus()
  {
    return serviceStatus;
  }

  public void setServiceStatus(int serviceStatus)
  {
    this.serviceStatus = serviceStatus;
  }

  public String getServiceOutput()
  {
    return serviceOutput;
  }

  public void setServiceOutput(String serviceOutput)
  {
    this.serviceOutput = serviceOutput;
  }

  public Integer getServiceResponseTime()
  {
    return serviceResponseTime;
  }

  public void setServiceResponseTime(Integer serviceResponseTime)
  {
    this.serviceResponseTime = serviceResponseTime;
  }
}

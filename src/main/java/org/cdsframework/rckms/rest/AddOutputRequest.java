package org.cdsframework.rckms.rest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

public class AddOutputRequest
{
  @Positive
  private int serviceStatus;
  @NotBlank
  private String serviceOutput;

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
}

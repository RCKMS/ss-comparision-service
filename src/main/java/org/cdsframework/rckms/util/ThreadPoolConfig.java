package org.cdsframework.rckms.util;

public class ThreadPoolConfig
{
  private int initialSize;
  private int maxSize;

  public int getInitialSize()
  {
    return initialSize;
  }

  public void setInitialSize(int initialSize)
  {
    this.initialSize = initialSize;
  }

  public int getMaxSize()
  {
    return maxSize;
  }

  public void setMaxSize(int maxSize)
  {
    this.maxSize = maxSize;
  }
}

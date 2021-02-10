package org.cdsframework.rckms;

import java.time.Duration;

import org.cdsframework.rckms.util.ThreadPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ConfigurationProperties(prefix = "processor")
@Qualifier("processorConfig")
public class ProcessorConfig
{
  private Duration queryFrequency;
  private boolean deleteOnComplete = true;

  @Bean
  @ConfigurationProperties(prefix = "processor.thread-pool")
  public ThreadPoolConfig queueProcessorExecutorConfig()
  {
    return new ThreadPoolConfig();
  }

  @Bean("queueProcessorExecutor")
  public TaskExecutor getQueueProcessorExecutor(@Qualifier("queueProcessorExecutorConfig") ThreadPoolConfig config)
  {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(config.getInitialSize());
    executor.setMaxPoolSize(config.getMaxSize());
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setThreadNamePrefix("Processor");
    return executor;
  }

  public Duration getQueryFrequency()
  {
    return queryFrequency;
  }

  public void setQueryFrequency(Duration queryFrequency)
  {
    this.queryFrequency = queryFrequency;
  }

  public boolean isDeleteOnComplete()
  {
    return deleteOnComplete;
  }

  public void setDeleteOnComplete(boolean deleteOnComplete)
  {
    this.deleteOnComplete = deleteOnComplete;
  }
}

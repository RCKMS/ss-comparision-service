package org.cdsframework.rckms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "org.cdsframework.rckms.dao")
public class SSComparisonServiceApplication
{

  public static void main(String[] args)
  {
    SpringApplication.run(SSComparisonServiceApplication.class, args);
  }

}

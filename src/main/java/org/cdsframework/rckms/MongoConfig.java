package org.cdsframework.rckms;

import java.time.Duration;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.cdsframework.rckms.dao.ServiceOutput;
import org.cdsframework.rckms.dao.util.MongoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;

@Configuration
public class MongoConfig
{

  private MongoTemplate mongoTemplate;

  @Value("${service-output-expiration}")
  Duration serviceOutputExpiration;

  public MongoConfig(MongoTemplate mongoTemplate)
  {
    this.mongoTemplate = mongoTemplate;
  }

  /**
   * Special handling for the TTL index on ServiceOutput.comparisonDate.
   * This checks to make sure the expiration is up-to-date in case it has changed in application.yml since
   * last startup. If it has changed, then it's dropped and recreated (can't just change the expiration).
   */
  @PostConstruct
  public void initIndexes()
  {
    String indexedField = "comparisonDate";
    String indexName = "comparisonDateTTL";

    Optional<IndexInfo> index = MongoUtils.findFieldIndex(mongoTemplate, ServiceOutput.class, indexedField);
    if (index.isPresent())
    {
      // Only drop/recreate the index if the expiration has changed
      if (index.get().getExpireAfter().isEmpty() || !index.get().getExpireAfter().get().equals(serviceOutputExpiration))
      {
        MongoUtils.dropIndex(mongoTemplate, ServiceOutput.class, index.get().getName());
        MongoUtils.addIndex(mongoTemplate, ServiceOutput.class, new Index()
            .named(indexName)
            .on("comparisonDate", Sort.Direction.ASC)
            .expire(serviceOutputExpiration));
      }
    }
    else
    {
      // Not already present, so just add it now
      MongoUtils.addIndex(mongoTemplate, ServiceOutput.class, new Index()
          .named(indexName)
          .on("comparisonDate", Sort.Direction.ASC)
          .expire(serviceOutputExpiration));
    }
  }

}


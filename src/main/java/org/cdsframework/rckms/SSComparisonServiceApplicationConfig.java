package org.cdsframework.rckms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cdsframework.rckms.dao.converter.OffsetDateTimeReadConverter;
import org.cdsframework.rckms.dao.converter.OffsetDateTimeWriteConverter;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableAsync
@EnableScheduling
@EnableCaching
public class SSComparisonServiceApplicationConfig
{
  public static final String CACHE_COMPARISON_TESTS = "comparison-tests";

  @Bean
  public MongoCustomConversions mongoCustomConversions()
  {
    List list = new ArrayList<>();
    list.add(new OffsetDateTimeReadConverter());
    list.add(new OffsetDateTimeWriteConverter());
    return new MongoCustomConversions(list);
  }

  @Bean
  public CacheManager cacheManager()
  {
    CaffeineCacheManager mgr = new CaffeineCacheManager();
    mgr.setCacheNames(Collections.singletonList(CACHE_COMPARISON_TESTS));
    Caffeine builder = Caffeine.newBuilder();
    mgr.setCaffeine(builder);
    return mgr;
  }

}

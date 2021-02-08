package org.cdsframework.rckms;

import java.util.ArrayList;
import java.util.List;

import org.cdsframework.rckms.dao.converter.OffsetDateTimeReadConverter;
import org.cdsframework.rckms.dao.converter.OffsetDateTimeWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
public class SSComparisonServiceApplicationConfig
{
  @Bean
  public MongoCustomConversions mongoCustomConversions() {
    List list = new ArrayList<>();
    list.add(new OffsetDateTimeReadConverter());
    list.add(new OffsetDateTimeWriteConverter());
    return new MongoCustomConversions(list);
  }
}

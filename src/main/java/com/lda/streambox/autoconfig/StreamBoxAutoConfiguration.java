package com.lda.streambox.autoconfig;

import com.lda.streambox.json.JsonConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class StreamBoxAutoConfiguration {

    @Bean
    JsonConverter jsonConverter(ObjectMapper objectMapper) {
        return new JsonConverter(objectMapper);
    }

}

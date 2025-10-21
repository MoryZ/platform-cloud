package com.old.silence.cloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;


@Configuration
@EnableSpringConfigured
public class SilenceConfigBootstrapConfiguration {

    @Bean
    PlatformConfigEnhancementApplicationContextInitializer silenceConfigEnhancementApplicationContextInitializer() {
        return new PlatformConfigEnhancementApplicationContextInitializer();
    }

}



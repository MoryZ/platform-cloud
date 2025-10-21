package com.old.silence.cloud.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;

public class PlatformDefaultConfigEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String DEFAULT_CONFIG_ENABLED_PROPERTY = "platform.config.default.enabled";

    private static final String DEFAULT_CONFIG_LOADED_PROPERTY = "platform.config.default.loaded";

    private static final String SPRING_CONFIG_ACTIVE_ON_PROFILE_PROPERTY = "spring.config.activate.on-profile";

    private static final String DEFAULT_CONFIG_FILE_PATH = "classpath:platform-default-config.yml";

    private static final String PLATFORM_DEFAULT_PROPERTY_SOURCE_NAME = "PlatformDefaultPropertySource";

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        if (!isDefaultConfigEnabled(environment) || isDefaultConfigLoaded(environment)) {
            return;
        }

        var resourceLoader = Objects.requireNonNullElseGet(application.getResourceLoader(), DefaultResourceLoader::new);
        var resource = resourceLoader.getResource(DEFAULT_CONFIG_FILE_PATH);
        var propertySourceLoader = new YamlPropertySourceLoader();

        List<PropertySource<?>> propertySources;
        try {
            propertySources = propertySourceLoader.load(PLATFORM_DEFAULT_PROPERTY_SOURCE_NAME, resource);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        propertySources = Objects.requireNonNullElseGet(propertySources, List::of);

        Collections.reverse(propertySources);

        for (var propertySource : propertySources) {
            var onProfile = (String) propertySource.getProperty(SPRING_CONFIG_ACTIVE_ON_PROFILE_PROPERTY);
            if (StringUtils.isEmpty(onProfile) || environment.acceptsProfiles(Profiles.of(onProfile))) {
                environment.getPropertySources().addLast(propertySource);
            }
        }
    }

    private static boolean isDefaultConfigEnabled(Environment environment) {
        return environment.getProperty(DEFAULT_CONFIG_ENABLED_PROPERTY, Boolean.class, true);
    }

    private static boolean isDefaultConfigLoaded(Environment environment) {
        return environment.getProperty(DEFAULT_CONFIG_LOADED_PROPERTY, Boolean.class, false);
    }
}

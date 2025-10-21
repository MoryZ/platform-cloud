package com.old.silence.cloud.config;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.ClassUtils;

public class PlatformConfigEnhancementApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static final String PLATFORM_ENHANCEMENT_PROPERTY_SOURCE_NAME = "PlatformEnhancementPropertySource";

    private static final String SPRING_JDBC_URL_PROPERTY = "spring.datasource.url";

    private static final String SPRING_JDBC_DRIVER_CLASS_NAME_PROPERTY = "spring.datasource.driver-class-name";

    private static final String MYSQL_JDBC_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";

    private static final String MYSQL_NEW_JDBC_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    private static final Set<String> PLATFORM_COMMON_MESSAGES_BASENAMES = Set.of("messages/platform_common_messages",
            "messages/platform_validation_messages");

    private static final String SPRING_MESSAGES_BASENAME_PROPERTY = "spring.messages.basename";

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

        var environment = applicationContext.getEnvironment();

        var properties = new HashMap<String, Object>();
        if (isMySqlJdbcDriverSetRequired(environment)) {
            properties.put(SPRING_JDBC_DRIVER_CLASS_NAME_PROPERTY, MYSQL_JDBC_DRIVER_CLASS_NAME);
        }
        properties.put(SPRING_MESSAGES_BASENAME_PROPERTY, getMessagesBasenameProperty(environment));

        var propertySource = new MapPropertySource(PLATFORM_ENHANCEMENT_PROPERTY_SOURCE_NAME, properties);
        environment.getPropertySources().addFirst(propertySource);
    }

    private static String getMessagesBasenameProperty(Environment environment) {

        var basenames = new LinkedHashSet<String>();
        var basenameString = environment.getProperty(SPRING_MESSAGES_BASENAME_PROPERTY);
        if (StringUtils.isNotBlank(basenameString)) {
            basenames.addAll(Set.of(basenameString.split(",")));
        }
        basenames.addAll(PLATFORM_COMMON_MESSAGES_BASENAMES);

        return String.join(",", basenames);
    }

    private static boolean isMySqlJdbcDriverSetRequired(Environment environment) {

        var driverClassName = environment.getProperty(SPRING_JDBC_DRIVER_CLASS_NAME_PROPERTY);
        if (StringUtils.isNotBlank(driverClassName)) {
            return false;
        }

        var jdbcUrl = environment.getProperty(SPRING_JDBC_URL_PROPERTY);
        var databaseDriver = DatabaseDriver.fromJdbcUrl(jdbcUrl);
        return databaseDriver == DatabaseDriver.MYSQL && ClassUtils.isPresent(MYSQL_JDBC_DRIVER_CLASS_NAME, null)
                && !ClassUtils.isPresent(MYSQL_NEW_JDBC_DRIVER_CLASS_NAME, null);
    }
}

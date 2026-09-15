package com.thoth.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for prod-profile config that must not silently inherit
 * an unauthenticated-surface exposure from the base application.yml.
 *
 * SEC-010: management.endpoint.health.show-details=always on the
 * unauthenticated /actuator/health endpoint must be overridden to "never"
 * in prod.
 *
 * SEC-017: springdoc/swagger-ui.enabled=true (base profile) exposes the
 * full API surface - every endpoint, every request/response schema - via
 * permitAll() /swagger-ui/** and /v3/api-docs/**. Fine for dev, unnecessary
 * reconnaissance surface in prod, so it must be disabled there.
 */
class ProdHealthExposureConfigTest {

    @Test
    void prodProfile_explicitlyDisablesHealthDetailExposure() {
        Properties props = loadProdProperties();

        assertEquals("never", props.getProperty("management.endpoint.health.show-details"));
    }

    @Test
    void prodProfile_explicitlyDisablesSwaggerUiAndApiDocs() {
        Properties props = loadProdProperties();

        assertEquals("false", props.getProperty("springdoc.api-docs.enabled"));
        assertEquals("false", props.getProperty("springdoc.swagger-ui.enabled"));
    }

    private Properties loadProdProperties() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource("application-prod.yml"));
        return factory.getObject();
    }
}

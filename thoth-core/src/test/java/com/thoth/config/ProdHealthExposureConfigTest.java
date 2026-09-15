package com.thoth.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression test for SEC-010: application.yml (base profile, inherited by
 * dev) sets management.endpoint.health.show-details=always on the
 * unauthenticated /actuator/health endpoint. application-prod.yml must
 * explicitly override this to "never" rather than silently inheriting it.
 */
class ProdHealthExposureConfigTest {

    @Test
    void prodProfile_explicitlyDisablesHealthDetailExposure() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource("application-prod.yml"));
        Properties props = factory.getObject();

        assertEquals("never", props.getProperty("management.endpoint.health.show-details"));
    }
}

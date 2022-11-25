package com.example.entrevueSpringBoot;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.cache.ContextCache;

import java.lang.annotation.*;

/**
 * Annotation for common configuration of integration tests.
 *
 * Having a common configuration for all integration tests prevent Spring from spawning
 * new ApplicationContext because of changing beans/properties/profiles, so running all
 * integration tests is faster.
 *
 * @see MergedContextConfiguration#hashCode that defines on which criterias reuse/respawn an ApplicationContext
 * @see ContextCache that caches ApplicationContext for reuse by multiple integration tests
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest
//@Import(ITConfiguration.class) // would contain beans shared by all integration tests
@AutoConfigureMockMvc // for EndpointsIT
public @interface IntegrationTest {
}

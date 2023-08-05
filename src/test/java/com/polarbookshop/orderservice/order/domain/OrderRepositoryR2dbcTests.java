package com.polarbookshop.orderservice.order.domain;

import com.polarbookshop.orderservice.config.DataConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

@DataR2dbcTest  //Identifies a test class that focuses on R2DBC components
@Import(DataConfig.class) //Imports R2DBC configuration needed to enable auditing
@Testcontainers //Activates automatic startup and cleanup of test containers
public class OrderRepositoryR2dbcTests {

    @Container //Identifies a PostgreSQL container for testing
    static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.4"));

    @Autowired
    private OrderRepository orderRepository;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        // Overwrites R2DBC and Flyway configuration to point to the test PostgreSQL instance
        registry.add("spring.r2dbc.url", OrderRepositoryR2dbcTests::r2dbcUrl);
        registry.add("spring.r2dbc.username", postgresql::getUsername);
        registry.add("spring.r2dbc.password", postgresql::getPassword);
        registry.add("spring.flyway.url", postgresql::getJdbcUrl);
    }

    private static String r2dbcUrl() {
        // Builds an R2DBC connection string, because Testcontainers doesn’t provide one out of the box as it
        // does for JDBC
        return String.format("r2dbc:postgresql://%s:%s/%s", postgresql.getHost(),
                postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT), postgresql.getDatabaseName());
    }

    @Test
    void createRejectedOrder() {
        var rejectedOrder = OrderService.buildRejectedOrder( "1234567890", 3);
        StepVerifier
                .create(orderRepository.save(rejectedOrder)) //Initializes a StepVerifier object with the object returned by OrderRepository
                .expectNextMatches(
                        order -> order.status().equals(OrderStatus.REJECTED)) //Asserts that the Order returned has the correct status
                .verifyComplete(); //Verifies that the reactive stream completed successfully
    }
}

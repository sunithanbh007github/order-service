package com.polarbookshop.orderservice.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "polar") //The prefix for the custom properties
public record ClientProperties(
        @NotNull
        URI catalogServiceUri //The property for specifying the Catalog Service URI. It cannot be null.
) {
}

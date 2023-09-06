package com.polarbookshop.orderservice.order.domain;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("orders") //Configures the mapping between the “Order” object and the “orders” table
public record Order(
        @Id
        Long id, //The primary key of the entity

        String bookIsbn,
        String bookName,
        Double bookPrice,
        Integer quantity,
        OrderStatus status,

        @CreatedDate
        Instant createdDate, //When the entity was created

        @LastModifiedDate
        Instant lastModifiedDate, //When the entity was modified the last time

        @CreatedBy
        String createdBy,

        @LastModifiedBy
        String lastModifiedBy,

        @Version
        int version //The entity’s version number
) {

    public static Order of(String bookIsbn, String bookName, Double bookPrice, Integer quantity, OrderStatus status) {
        return new Order(null, bookIsbn, bookName, bookPrice, quantity, status, null, null, null, null,0);
    }
}

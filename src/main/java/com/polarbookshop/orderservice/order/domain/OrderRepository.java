package com.polarbookshop.orderservice.order.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;


//Extends a reactive repository providing CRUD operations, specifies the type of managed entity (Order), and its primary key type (Long)
public interface OrderRepository
        extends ReactiveCrudRepository<Order,Long> {
    Flux<Order> findAllByCreatedBy(String userId); //Custom method to query only the orders created by the given user
}


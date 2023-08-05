package com.polarbookshop.orderservice.order.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;


//Extends a reactive repository providing CRUD operations, specifies the type of managed entity (Order), and its primary key type (Long)
public interface OrderRepository
        extends ReactiveCrudRepository<Order,Long> { }


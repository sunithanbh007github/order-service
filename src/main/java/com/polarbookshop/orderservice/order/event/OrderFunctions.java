package com.polarbookshop.orderservice.order.event;

import com.polarbookshop.orderservice.order.domain.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Configuration
public class OrderFunctions {

    private static final Logger log = LoggerFactory.getLogger(OrderFunctions.class);

    @Bean
    public Consumer<Flux<OrderDispatchedMessage>> dispatchOrder(OrderService orderService) {
        return flux ->
                orderService.consumeOrderDispatchedEvent(flux) //For each dispatched message, it updates the related order in the database.
                .doOnNext(order -> log.info("The order with id {} is dispatched",
                        order.id())) //For each order updated in the database, it logs a message.
                .subscribe();//Subscribes to the reactive stream in order to activate it. Without a subscriber, no data flows through
                             // the stream.
    }
}

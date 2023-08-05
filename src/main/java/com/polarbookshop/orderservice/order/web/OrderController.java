package com.polarbookshop.orderservice.order.web;

import com.polarbookshop.orderservice.order.domain.Order;
import com.polarbookshop.orderservice.order.domain.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController //Stereotype annotation marking a class as a Spring component and a source of handlers for REST endpoints
@RequestMapping("orders") //Identifies the root path mapping URI for which the class provides handlers (/orders)
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Flux<Order> getAllOrders() { //A Flux is used to publish multiple orders (0..N).
        return orderService.getAllOrders();
    }

    @PostMapping
    public Mono<Order> submitOrder(
            @RequestBody @Valid OrderRequest orderRequest) { //Accepts an OrderRequest object, validated and used to create an order.
        //The created order is returned as a Mono.
        return orderService.submitOrder(orderRequest.isbn(), orderRequest.quantity());
    }
}

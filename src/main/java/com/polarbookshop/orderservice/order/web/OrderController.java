package com.polarbookshop.orderservice.order.web;

import com.polarbookshop.orderservice.order.domain.Order;
import com.polarbookshop.orderservice.order.domain.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController //Stereotype annotation marking a class as a Spring component and a source of handlers for REST endpoints
@RequestMapping("orders") //Identifies the root path mapping URI for which the class provides handlers (/orders)
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping //Autowires the JWT representing the currently authenticated user
    public Flux<Order> getAllOrders(@AuthenticationPrincipal Jwt jwt) { //A Flux is used to publish multiple orders (0..N).
        log.info("Fetching all orders");
        return orderService.getAllOrders(jwt.getSubject()); //Extracts the subject of the JWT and uses it as the user identifier
    }

    @PostMapping
    public Mono<Order> submitOrder(
            @RequestBody @Valid OrderRequest orderRequest) { //Accepts an OrderRequest object, validated and used to create an order.
        log.info("Order for {} copies of the book with ISBN {}", orderRequest.quantity(), orderRequest.isbn());
        //The created order is returned as a Mono.
        return orderService.submitOrder(orderRequest.isbn(), orderRequest.quantity());
    }
}

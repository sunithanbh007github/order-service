package com.polarbookshop.orderservice.order.domain;

import com.polarbookshop.orderservice.book.Book;
import com.polarbookshop.orderservice.book.BookClient;
import com.polarbookshop.orderservice.order.event.OrderAcceptedMessage;
import com.polarbookshop.orderservice.order.event.OrderDispatchedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service //Stereotype annotation that marks a class to be a service managed by Spring
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final BookClient bookClient;
    private final OrderRepository orderRepository;

    private final StreamBridge streamBridge;

    public OrderService(BookClient bookClient, OrderRepository orderRepository, StreamBridge streamBridge) {
        this.bookClient = bookClient;
        this.orderRepository = orderRepository;
        this.streamBridge = streamBridge;
    }

    public Flux<Order> getAllOrders() { //A Flux is used to publish multiple orders (0..N)
        return orderRepository.findAll();
    }

    @Transactional //Executes the method in a local transaction
    public Mono<Order> submitOrder(String isbn, int quantity) {
        return bookClient.getBookByIsbn(isbn) //Calls the Catalog Service to check the book’s availability
                .map(book -> buildAcceptedOrder(book, quantity)) //If the book is available, it accepts the order.
                .defaultIfEmpty(buildRejectedOrder(isbn, quantity)) //If the book is not available, it rejects the order.
                .flatMap(orderRepository::save) //Saves the order in the database(either as accepted or rejected)
                .doOnNext(this::publishOrderAcceptedEvent); //Publishes an event if the order is accepted

    }

    public static Order buildAcceptedOrder(Book book, int quantity) {
        //When an order is accepted, we specify ISBN, book name (title + author), quantity, and status.
        //Spring Data takes care of adding the identifier, version, and audit metadata.
        return Order.of(book.isbn(), book.title() + " - " + book.author(),
                book.price(), quantity, OrderStatus.ACCEPTED);
    }

    public static Order buildRejectedOrder(String bookIsbn, int quantity) {
        //When an order is rejected, we only specify ISBN, quantity, and status. Spring Data takes care of
        //adding identifier, version, and audit metadata.
        return Order.of(bookIsbn, null, null, quantity, OrderStatus.REJECTED);
    }

    private void publishOrderAcceptedEvent(Order order) {
        if (!order.status().equals(OrderStatus.ACCEPTED)) {
            return; //If the order is not accepted, it does nothing.
        }
        var orderAcceptedMessage =
                new OrderAcceptedMessage(order.id()); //Builds a message to notify that an order has been accepted
        log.info("Sending order accepted event with id: {}", order.id());
        var result = streamBridge.send("acceptOrder-out-0",
                orderAcceptedMessage); //Explicitly sends a message to the acceptOrder-out-0 binding
        log.info("Result of sending data for order with id {}: {}", order.id(), result);
    }

    public Flux<Order> consumeOrderDispatchedEvent(Flux<OrderDispatchedMessage> flux) {
        //Accepts a reactive stream of OrderDispatchedMessage objects as input
        return flux
                .flatMap(message -> orderRepository.findById(message.orderId())) //For each object emitted to the stream,
                                                                                // it reads the related order from the database.
                .map(this::buildDispatchedOrder) //Updates the order with the “dispatched” status
                .flatMap(orderRepository::save); //Saves the updated order in the database
    }

    private Order buildDispatchedOrder(Order existingOrder) {
        return new Order( //Given an order, it returns a new record with the “dispatched” status.
                existingOrder.id(),
                existingOrder.bookIsbn(),
                existingOrder.bookName(),
                existingOrder.bookPrice(),
                existingOrder.quantity(),
                OrderStatus.DISPATCHED,
                existingOrder.createdDate(),
                existingOrder.lastModifiedDate(),
                existingOrder.version()
        );
    }
}

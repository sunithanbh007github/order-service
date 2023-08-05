package com.polarbookshop.orderservice.order.domain;

import com.polarbookshop.orderservice.book.Book;
import com.polarbookshop.orderservice.book.BookClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service //Stereotype annotation that marks a class to be a service managed by Spring
public class OrderService {

    private final BookClient bookClient;
    private final OrderRepository orderRepository;

    public OrderService(BookClient bookClient, OrderRepository orderRepository) {
        this.bookClient = bookClient;
        this.orderRepository = orderRepository;
    }

    public Flux<Order> getAllOrders() { //A Flux is used to publish multiple orders (0..N)

        return orderRepository.findAll();
    }

    public Mono<Order> submitOrder(String isbn, int quantity) {
        return bookClient.getBookByIsbn(isbn) //Calls the Catalog Service to check the book’s availability
                .map(book -> buildAcceptedOrder(book, quantity)) //If the book is available, it accepts the order.
                .defaultIfEmpty(buildRejectedOrder(isbn, quantity)) //If the book is not available, it rejects the order.
                .flatMap(orderRepository::save); //Saves the order (either as accepted or rejected)

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
}

package com.polarbookshop.orderservice.book;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class BookClient {

    private static final String BOOKS_ROOT_API = "/books/";
    private final WebClient webClient;

    public BookClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Book> getBookByIsbn(String isbn) {
        return webClient
                .get() //The request should use the GET method.
                .uri(BOOKS_ROOT_API + isbn) //The target URI of the request is /books/{isbn}
                .retrieve()  //Sends the request and retrieves the response
                .bodyToMono(Book.class) //Returns the retrieved object as Mono<Book>
                .timeout(Duration.ofSeconds(30), //Sets a 3-second timeout for the GET request.
                        Mono.empty()) // The fallback returns an empty Mono object.
                .onErrorResume(WebClientResponseException.NotFound.class, exception -> Mono.empty())
                                                                            //Returns an empty object when a 404 response is received
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))) //Exponential backoff is used as the retry strategy.
                                                        //Three attempts are allowed with a 100 ms initial backoff.
                .onErrorResume(Exception.class, exception -> Mono.empty());
                //If any error happens after the 3 retry attempts, catch the exception and return an empty object.

    }
}

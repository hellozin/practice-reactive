package me.hellozin.reactive.ch04;

import java.time.Duration;
import java.util.Random;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Transaction {

    private static final Logger log = LoggerFactory.getLogger(Transaction.class);

    private static final Random random = new Random();
    private final int id;

    public Transaction(int id) {
        this.id = id;
        log.info("[T: {}] created", id);
    }

    public static Mono<Transaction> beginTransaction() {
        return Mono.defer(() ->
                Mono.just(new Transaction(random.nextInt(1000))));
    }

    public Flux<String> insertRows(Publisher<String> rows) {
        return Flux.from(rows)
                .delayElements(Duration.ofMillis(100))
                .flatMap(row -> {
                    if (random.nextInt(10) < 2) {
                        return Mono.error(new RuntimeException("Error: " + row));
                    } else {
                        return Mono.just(row);
                    }
                });
    }

    public Mono<Void> commit() {
        return Mono.defer(() -> {
            log.info("[T: {}] commit", id);
            if (random.nextBoolean()) {
                return Mono.empty();
            } else {
                return Mono.error(new RuntimeException("Conflict"));
            }
        });
    }

    public Mono<Void> rollback() {
        return Mono.defer(() -> {
            log.info("[T: {}] rollback", id);
            if (random.nextBoolean()) {
                return Mono.empty();
            } else {
                return Mono.error(new RuntimeException("Connection error"));
            }
        });
    }
}

package me.hellozin.reactive.ch04;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class FluxAndMonoTest {

    @Test
    void flux() {
        Flux<String> stream1 = Flux.just("hello", "world");
        Flux<Integer> stream2 = Flux.fromArray(new Integer[]{1, 2, 3});
        Flux<Integer> stream3 = Flux.fromIterable(Arrays.asList(4, 5, 6));
        Flux<Integer> stream4 = Flux.range(10, 5);
    }

    @Test
    void mono() {
        Mono<String> stream1 = Mono.just("one");
        Mono<String> stream2 = Mono.justOrEmpty(null);
        Mono<String> stream3 = Mono.justOrEmpty(Optional.empty());
        Mono.fromCallable(() -> mockValidate("name"));
    }

    @Test
    void emptyAndError() {
        Flux<Object> empty = Flux.empty();
        Flux<Object> never = Flux.never(); // 완료 또는 에러 신호를 보내지 않는다.
        Mono<Object> error = Mono.error(new RuntimeException("error"));
    }

    @Test
    void deferMono() {
        String userId = "100User";
        Mono.defer(() -> // 구독이 발생했을 때만 동작한다.
                userId.contains("User")
                        ? Mono.fromCallable(() -> mockValidate(userId))
                        : Mono.error(new RuntimeException("Invalid"))
        );
    }

    @Test
    void block() {
        Flux.just("user1", "user2", "user3")
                .flatMap(user -> requestBooks(user)
                    .map(book -> user + "/" + book))
                .subscribe(System.out::println);
    }

    public Flux<String> requestBooks(String user) {
        return Flux.range(1, 3)
                .map(i -> "book-" + i)
                .delayElements(Duration.ofMillis(3));
    }

    Boolean mockValidate(String input) {
        System.out.println("Exec mockValidate");
        return Boolean.TRUE;
    }

}

package me.hellozin.reactive.ch04;

/*
 여러 외부 서비스와 통신하는 경우 다양한 예외 상황에 대응하도록 리액티브 시스템을 구성해야 한다.
 onError는 의미상(?) 종료 시점에 처리하는 연산으로, 수행 시 리액티브 시퀸스는 중단된다.

 에러를 핸들링 하는 방법에는 아래와 같은 것들이 있다.
 - onError : catch and termination
 - onErrorReturn : catch and replace error
 - onErrorResume : catch and execute other workflow
 - onErrorMap : catch and replace error to more specific error
 - retry : catch and retry (resubscribe source reactive stream)
 - retryBackoff : retry with increasing delay

 빈 스트림은 아래와 같이 처리할 수 있다.
 - defaultIfEmpty or switchIfEmpty

 또 다른 유용한 기능으로는 timeout 이 있다.
 - timeout
*/

import static me.hellozin.reactive.Sleep.sleep;

import java.time.Duration;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

class HandlingErrorTest {

    private static final Logger log = LoggerFactory.getLogger(HandlingErrorTest.class);
    private static final Random random = new Random();

    @Test
    void handling() {
        Flux.just("user-1")
                .flatMap(user ->
                        recommendedBooks(user)
                                .retryBackoff(5, Duration.ofMillis(100))
                                .timeout(Duration.ofSeconds(3))
                                .onErrorResume(e -> Flux.just("The Martian"))
                ).subscribe(
                        b -> log.info("onNext: {}", b),
                        e -> log.warn("onError: {}", e.getMessage()),
                        () -> log.info("onComplete")
                );

        sleep(5000);
    }

    private Flux<String> recommendedBooks(String userId) {
        return Flux.defer(() -> {
            if (random.nextInt(10) < 7) {
                return Flux.<String>error(new RuntimeException("Error"))
                        .delaySequence(Duration.ofMillis(100));
            } else {
                return Flux.just("Blue Mars", "The Expanse")
                        .delayElements(Duration.ofMillis(50));
            }
        }).doOnSubscribe(s -> log.info("Request for {}", userId));
    }

}

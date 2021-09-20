package me.hellozin.reactive.ch04;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

public class FactoryMethods {

    private static final Logger log = LoggerFactory.getLogger(FactoryMethods.class);

    private Consumer<IntStream> consumer;
    private final Consumer<Object> onNextLoggingConsumer = item -> log.info("onNext: {}", item);

    // Creating streams programmatically
    // Let's know about reactive factory methods
    // push, create, generate, using, usingWhen
    // Do not care about backpressure and cancellation

    @Test
    void previous() {
        // Create reactive streams of arrays, futures, and blocking requests.
        Flux.just(1, 2, 3);

        Flux.range(1, 10)
                .delayElements(Duration.ofMillis(1))
                .subscribe(onNextLoggingConsumer);
        sleep(1000);
    }

    @Test
    void fluxPush() {
        Flux.push(sink -> consumer = items ->
                        items.forEach(sink::next))
                .delayElements(Duration.ofMillis(1)) // backpressure situation
                .subscribe(onNextLoggingConsumer);

        consumer.accept(IntStream.range(1, 10));

        sleep(100);
    }

    @Test
    void fluxPushMultiSource() {
        Flux.push(emitter -> consumer = items ->
                        items.forEach(emitter::next))
                .subscribe(onNextLoggingConsumer);

        Thread t1 = new Thread(() -> consumer.accept(IntStream.range(1, 5)));
        Thread t2 = new Thread(() -> consumer.accept(IntStream.range(5, 10)));

        t1.start();
        t2.start();

        sleep(10000);
    }

    // public static <T> Flux<T> create(Consumer<? super FluxSink<T>> emitter)
    @Test
    void fluxCreate() {
        // FluxSink를 추가로 직렬화 하기 때문에 서로다른 스레드에서 발생한 이벤트도 처리가 가능
        Disposable disposed = Flux.create(sink -> {
                    sink.onDispose(() -> log.info("Disposed"));
                    consumer = items -> items.forEach(sink::next);
                })
                .subscribe(onNextLoggingConsumer);


        Thread t1 = new Thread(() -> consumer.accept(IntStream.range(1, 5)));
        Thread t2 = new Thread(() -> consumer.accept(IntStream.range(5, 10)));

        t1.start();
        t2.start();

        log.info("Disposed? {}", disposed.isDisposed());

        sleep(1000);

        disposed.dispose();
        log.info("Disposed? {}", disposed.isDisposed());
    }

    // public static <T, S> Flux<T> generate(Callable<S> stateSupplier, BiFunction<S, SynchronousSink<T>, S> generator)
    @Test
    void fluxGenerate() {
        Flux.generate(
                        () -> Tuples.of(0L, 1L),
                        (state, sink) -> {
                            log.info("generated value: {}", state.getT2());
                            sink.next(state.getT2());
                            long newValue = state.getT1() + state.getT2();
                            return Tuples.of(state.getT2(), newValue);
                        })
                .delayElements(Duration.ofMillis(1))    // delay가 있으면 sync 하게 구독하지 않는다
                .take(7)
                .subscribe(onNextLoggingConsumer);

        sleep(1000);
    }

    // Wrapping disposable resources into Reactive Streams
    @Test
    void connectionWithoutReactive() {
        try (Connection connection = Connection.newConnection()) {
            connection.getData().forEach(
                    // process data
                    data -> log.info("Received data: {}", data)
            );
        } catch (Exception e) {
            log.info("Error: {}", e.getMessage());
        }
    }

    // public static <T, D> Flux<T> using(Callable<? extends D> resourceSupplier,
    //          Function<? super D, ? extends Publisher<? extends T>> sourceSupplier, Consumer<? super D> resourceCleanup)
    @Test
    void fluxUsing() {
        Flux<String> ioRequestResults = Flux.using(
                Connection::newConnection,
                connection -> Flux.fromIterable(connection.getData()),
                Connection::close
        );

        ioRequestResults.subscribe(
                data -> log.info("Received data {}", data),
                e -> log.info("Error: {}", e.getMessage()),
                () -> log.info("Stream finished"));
    }

    // TODO: fix it
//    @Test
//    void fluxUsingWhen() {
//        Flux.usingWhen(
//                Transaction.beginTransaction(),
//                transaction -> transaction.insertRow(Flux.just("A")),
//                Transaction::commit,
//                Transaction::rollback,
//                Transaction::rollback
//        ).subscribe(
//                d -> log.info("onNext: {}", d),
//                e -> log.info("onError: {}", e.getMessage()),
//                () -> log.info("onComplete")
//        );
//    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
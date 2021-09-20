package me.hellozin.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class PracticeReactiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(PracticeReactiveApplication.class, args);
    }

}

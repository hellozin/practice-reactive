package me.hellozin.reactive.ch04;

import java.util.Arrays;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connection implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Connection.class);

    private final Random random = new Random();

    public Iterable<String> getData() {
        if (random.nextInt(10) < 3) {
            throw new RuntimeException("Communication error");
        }
        return Arrays.asList("Some", "Data");
    }

    @Override
    public void close() {
        log.info("IO Connection closed");
    }

    public static Connection newConnection() {
        log.info("IO Connection created");
        return new Connection();
    }
}

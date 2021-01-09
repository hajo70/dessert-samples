package de.spricom.dessert.jdeps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class LoggerTest {
    private static final Logger log = LogManager.getLogger(LoggerTest.class);

    /**
     * Use this to check the logger configuration.
     */
    @Test
    void testLevel() {
        log.trace("test");
        log.debug("test");
        log.info("test");
        log.warn("test");
        log.error("test");
        log.fatal("test");
    }

    @Test
    void testParams() {
        log.info("test val1 {} val2 {}", 17, 42);
        log.info("test val1 %s val2 %d", "xx", 42);
        log.debug("test {} with exception", 42, new Exception("dummy"));
    }
}

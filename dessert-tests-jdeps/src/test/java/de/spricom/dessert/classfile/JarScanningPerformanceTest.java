package de.spricom.dessert.classfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class has been written to get an idea how long it needs to scan and read the entries of
 * JAR files. It shows that reading the entry-list is quite fast, but reading the content of
 * each entry needs some time. It shows, when analyzing classes more than half of the time is
 * used to de-compress the class files.
 */
public class JarScanningPerformanceTest {
    private static final Logger log = LogManager.getLogger(JarScanningPerformanceTest.class);

    long ts = System.nanoTime();
    private int rootCounter;
    private int classCounter;

    @AfterEach
    public void showStatistics(TestInfo info) {
        long durationNanos = System.nanoTime() - ts;
        double classesPerSecond = 1e9 * classCounter / durationNanos;
        log.info("Results of {}:\n{}\n{}\n{}", info.getDisplayName(),
                String.format("%36s: %8d", "number of jar files or directories", rootCounter),
                String.format("%36s: %8d", "total number of classes", classCounter),
                String.format("%36s: %8.0f", "classes per second", classesPerSecond));
    }

    @Test
    public void testScanEntries() throws IOException {
        processClassPath(this::scanEntriesOfJarFile);
    }

    @Test
    public void testScanEntriesAndContent() throws IOException {
        processClassPath(this::scanEntriesAndContentOfJarFile);
    }

    private void processClassPath(Consumer<File> scan) {
        long start = System.currentTimeMillis();
        for (String file : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (file.toLowerCase().endsWith(".jar")) {
                scan.accept(new File(file));
                rootCounter++;
            }
        }
    }

    private void scanEntriesOfJarFile(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    classCounter++;
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error processing " + file, ex);
        }
    }

    private void scanEntriesAndContentOfJarFile(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    try (InputStream is = jarFile.getInputStream(entry)) {
                        byte[] content = is.readAllBytes();
                        assertThat(content).isNotEmpty();
                    }
                    classCounter++;
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error processing " + file, ex);
        }
    }
}

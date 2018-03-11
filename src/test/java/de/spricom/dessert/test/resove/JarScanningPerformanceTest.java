package de.spricom.dessert.test.resove;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import static org.fest.assertions.Assertions.assertThat;

/**
 * This class has been written to get an idea how long it needs to scan and read the entries of
 * JAR files. It shows that reading the entry-list is quite fast, but reading the content of
 * each entry needs some time. It shows, when analyzing classes more than half of the time is
 * used to de-compress the class files.
 */
public class JarScanningPerformanceTest {
    private static final Logger logger = Logger.getLogger(JarScanningPerformanceTest.class.getName());

    private int classesCount;
    private int jarsCount;
    private long total;

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
                jarsCount++;
            }
        }
        total = System.currentTimeMillis() - start;
        logger.info(this::report);
    }

    private void scanEntriesOfJarFile(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    classesCount++;
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
                    classesCount++;
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error processing " + file, ex);
        }
    }

    private String report() {
        StringBuilder sb = new StringBuilder();
        sb.append("Needed " + total + " ms to scan " + classesCount + " classes in " + jarsCount + " JAR files.\n");
        return sb.toString();
    }

}

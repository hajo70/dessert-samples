package de.spricom.dessert.test.classfile;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.traversal.ClassVisitor;
import de.spricom.dessert.traversal.PathProcessor;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.fest.assertions.Assertions.assertThat;

/**
 * This test checks the performance of the class analyzing code.
 */
public class PerformanceTest implements ClassVisitor {
    private static final Logger logger = Logger.getLogger(PerformanceTest.class.getName());

    private int count;
    private long sum;
    private long max;
    private long total;

    private File maxRoot;
    private String maxClassname;

    @Test
    public void testTraverseSystemClassPath() throws IOException {
        long start = System.currentTimeMillis();
        PathProcessor proc = new PathProcessor();
        proc.traverseAllClasses(this);
        total = System.currentTimeMillis() - start;
        logger.info(this::report);
    }

    private String report() {
        StringBuilder sb = new StringBuilder();
        sb.append("Needed " + total + " ms to analyze " + count + " classes.\n");
        double percent = ((double) (sum / 10000)) / total;
        sb.append(String.format("%3.1f %% processing time need for class analyzation.%n", percent));
        long avg = sum / (count * 1000);
        sb.append(String.format("Average time per class: %d µs.%n", avg));
        sb.append(String.format("Maximum of %d µs for %s %nin %s.%n", max / 1000, maxClassname, maxRoot));

        assertThat(count).as("At least 10000 classes should by analyzed.").isGreaterThan(10000);
        assertThat(avg).as("Average time should be below 200 µs.").isLessThan(200);

        return sb.toString();
    }

    /**
     * Measures the time for reading (and de-compressing) and analyzing the
     * class file.
     */
    @Override
    public void visit(File root, String classname, InputStream content) {
        try {
            long start = System.nanoTime();
            ClassFile cf = new ClassFile(content);
            long delta = System.nanoTime() - start;
            sum += delta;
            count++;

            if (delta > max) {
                max = delta;
                maxRoot = root;
                maxClassname = classname;
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE,"Unable to analyze " + classname + " in " + root, ex);
        }
    }
}

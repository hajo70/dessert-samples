package de.spricom.dessert.classfile;

import de.spricom.dessert.traversal.ClassVisitor;
import de.spricom.dessert.traversal.PathProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DessertPerformanceTest implements ClassVisitor {
    private static final Logger log = LogManager.getLogger(DessertPerformanceTest.class);

    long ts = System.nanoTime();
    private int rootCounter;
    private long classCounter;

    @AfterEach
    public void showStatistics() {
        long durationNanos = System.nanoTime() - ts;
        double classesPerSecond = 1e9 * classCounter / durationNanos;
        log.info("Results:\n{}\n{}\n{}",
                String.format("%36s: %8d", "number of jar files or directories", rootCounter),
                String.format("%36s: %8d", "total number of classes", classCounter),
                String.format("%36s: %8.0f", "classes per second", classesPerSecond));
    }

    @Test
    void testProjectClasses() {
        PathProcessor proc = new PathProcessor() {
            @Override
            protected void processJar(File file, ClassVisitor visitor) {
            }

            @Override
            protected void processDirectory(File file, ClassVisitor visitor) throws IOException {
                rootCounter++;
                super.processDirectory(file, visitor);
            }
        };
        check(proc);
    }

    @Test
    public void testJarsOnClassPath() {
        PathProcessor proc = new PathProcessor() {
            @Override
            protected void processJar(File file, ClassVisitor visitor) throws IOException {
                rootCounter++;
                super.processJar(file, visitor);
            }

            @Override
            protected void processDirectory(File file, ClassVisitor visitor) {
            }
        };
        check(proc);
    }

    private void check(PathProcessor proc) {
        proc.traverseAllClasses(this);
    }

    @Override
    public void visit(File root, String classname, InputStream content) {
        classCounter++;
        try {
            ClassFile cf = new ClassFile(content);
            Set<String> cfdeps = cf.getDependentClasses();
            assertThat(cfdeps)
                    .as("%s[%s]", classname, root)
                    .isNotNull();
        } catch (IOException ex) {
            throw new RuntimeException("Processing " + classname + " in " + root.getAbsolutePath() + " failed.", ex);
        }
    }
}

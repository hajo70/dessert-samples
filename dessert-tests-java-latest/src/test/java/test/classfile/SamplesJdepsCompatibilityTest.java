package test.classfile;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.jdeps.JdepsWrapper;
import de.spricom.dessert.traversal.ClassVisitor;
import de.spricom.dessert.traversal.PathProcessor;
import de.spricom.dessert.util.SetHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.logging.Logger;

import static org.fest.assertions.Fail.fail;

public class SamplesJdepsCompatibilityTest implements ClassVisitor {
    private static final File JDK_8_HOME = new File("C:\\Program Files\\Java\\jdk1.8.0_161");
    private static final File JDK_9_HOME = new File("C:\\Program Files\\Java\\jdk-9.0.4");
    private static final Logger logger = Logger.getLogger(SamplesJdepsCompatibilityTest.class.getName());
    private JdepsWrapper wrapper;

    @Before
    public void init() {
        wrapper = new JdepsWrapper(new File("."));
        if (JDK_9_HOME.exists()) {
            wrapper.setJdepsCommand(new File(JDK_9_HOME, "bin/jdeps").getAbsolutePath());
        } else if (JDK_8_HOME.exists()) {
            wrapper.setJdepsCommand(new File(JDK_8_HOME, "bin/jdeps").getAbsolutePath());
        }
        // The default is to use jdeps on path if neither JDK-8 nor JDK-9 has been found in the dirs above.
        wrapper.setVerbose(false);
    }

    @Test
    public void testJarsOnClassPath() throws IOException {
        PathProcessor proc = new PathProcessor() {
            @Override
            protected void processJar(File file, ClassVisitor visitor) throws IOException {
                refresh(file);
                super.processJar(file, visitor);
            }

            @Override
            protected void processDirectory(File file, ClassVisitor visitor) throws IOException {
                return;
            }
        };
        check(proc);
    }

    private void check(PathProcessor proc) throws IOException {
        proc.traverseAllClasses(this);
    }

    private void refresh(File path) {
        System.out.println("Checking " + path);
        wrapper.setPath(path);
        try {
            wrapper.refresh();
        } catch (IOException ex) {
            throw new RuntimeException("Processing " + path + " failed.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void visit(File root, String classname, InputStream content) {
        try {
            ClassFile cf = new ClassFile(content);
            Set<String> cfdeps = cf.getDependentClasses();
            Set<String> jdeps = wrapper.getDependencies(classname);
            assertDependenciesMatch(root, classname, cfdeps, jdeps, cf);
        } catch (IOException ex) {
            throw new RuntimeException("Processing " + classname + " in " + root.getAbsolutePath() + " failed.", ex);
        }
    }

    private void assertDependenciesMatch(File root, String classname, Set<String> cfdeps, Set<String> jdeps, ClassFile cf) {
        if (cfdeps.equals(jdeps)) {
            return;
        }
        if (!SetHelper.containsAll(cfdeps, jdeps)) {
            logger.info("Dump of jdeps-dependencies:\n" + dump(jdeps));
            fail("Dependencies of " + classname + " in " + root + " don't contain " + SetHelper.subtract(jdeps, cfdeps)
                    + "\ndessert: " + cfdeps
                    + "\n  jdeps: " + jdeps);
        }
        // See https://bugs.openjdk.java.net/browse/JDK-8134625.
        Set<String> diff = SetHelper.subtract(cfdeps, jdeps);
        logger.info("Additional dependencies detected for " + classname + " in " + root + ": " + diff
                + "\ndessert: " + cfdeps
                + "\n  jdeps: " + jdeps);
    }

    private String dump(Set<String> deps) {
        StringBuilder sb = new StringBuilder();
        for (String cn : deps) {
            sb.append("\"").append(cn).append("\",\n");
        }
        return sb.toString();
    }
}

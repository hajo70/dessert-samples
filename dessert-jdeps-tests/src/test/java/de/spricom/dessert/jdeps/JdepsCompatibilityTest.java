package de.spricom.dessert.jdeps;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.classfile.FieldInfo;
import de.spricom.dessert.classfile.MethodInfo;
import de.spricom.dessert.classfile.attribute.*;
import de.spricom.dessert.traversal.ClassVisitor;
import de.spricom.dessert.traversal.PathProcessor;
import de.spricom.dessert.util.LookupUtil;
import de.spricom.dessert.util.SetHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class JdepsCompatibilityTest implements ClassVisitor {
    private static final Logger log = LogManager.getLogger(JdepsCompatibilityTest.class);

    private final JdepsWrapper wrapper = new JdepsWrapper();
    private JdepsResult jdepsResult;

    @BeforeEach
    void init() {
        wrapper.addOptions("--multi-release", "base");
    }

    @Test
    void testProjectClasses() throws IOException {
        PathProcessor proc = new PathProcessor() {
            @Override
            protected void processJar(File file, ClassVisitor visitor) {
            }

            @Override
            protected void processDirectory(File file, ClassVisitor visitor) throws IOException {
                analyze(file);
                super.processDirectory(file, visitor);
            }
        };
        check(proc);
    }

    @Test
    public void testJarsOnClassPath() throws IOException {
        PathProcessor proc = new PathProcessor() {
            @Override
            protected void processJar(File file, ClassVisitor visitor) throws IOException {
                if (skip(file)) {
                    log.warn(() -> "Skipping " + file.getAbsolutePath());
                    return;
                }
                analyze(file);
                super.processJar(file, visitor);
            }

            @Override
            protected void processDirectory(File file, ClassVisitor visitor) {
            }
        };
        check(proc);
    }

    private boolean skip(File jarFile) {
        return Set.of(
                "junit-platform-launcher-1.7.0.jar",
                "log4j-api-2.14.0.jar",
                "log4j-core-2.14.0.jar",
                "junit-jupiter-api-5.7.0.jar",
                "junit-platform-commons-1.7.0.jar",
                "junit-jupiter-engine-5.7.0.jar",
                "junit-platform-engine-1.7.0.jar",
                "assertj-core-3.18.1.jar"
        ).contains(jarFile.getName());
    }

    private void analyze(File root) {
        log.info("Analyzing {}", root);
        try {
            jdepsResult = wrapper.analyze(root);
        } catch (IOException ex) {
            throw new RuntimeException("Processing " + root + " failed.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }


    private void check(PathProcessor proc) throws IOException {
        proc.traverseAllClasses(this);
    }

    @Override
    public void visit(File root, String classname, InputStream content) {
        try {
            log.debug("Checking {}[{}]", classname, root.getName());
            ClassFile cf = new ClassFile(content);
            Set<String> cfdeps = cf.getDependentClasses();
            Set<String> jdeps = jdepsResult.getDependencies(classname);

            String name = classname + "[" + root.getName() + "]";
            assertThat(cfdeps).as(name).containsAll(jdeps);

            if (cfdeps.size() != jdeps.size()) {
                log.info(() -> "Dessert found additional dependencies for " + name + ":\n" + diff(cfdeps, jdeps));
            }
            // assertDependenciesMatch(root, classname, cfdeps, jdeps, cf);
        } catch (IOException ex) {
            throw new RuntimeException("Processing " + classname + " in " + root.getAbsolutePath() + " failed.", ex);
        }
    }

    private String diff(Set<String> cfdeps, Set<String> jdeps) {
        return SetHelper.subtract(cfdeps, jdeps).stream().collect(Collectors.joining("\n"));
    }

    private void assertDependenciesMatch(File root, String classname, Set<String> cfdeps, Set<String> jdeps, ClassFile cf) {
        if (cfdeps.equals(jdeps)) {
            return;
        }
        if (!SetHelper.containsAll(cfdeps, jdeps)) {
            log.info("Dump of jdeps-dependencies:\n" + dump(jdeps));
            fail("Dependencies of " + classname + " in " + root + " don't contain " + SetHelper.subtract(jdeps, cfdeps)
                    + "\ndessert: " + cfdeps
                    + "\n  jdeps: " + jdeps);
        }
        // See https://bugs.openjdk.java.net/browse/JDK-8134625.
        Set<String> diff = SetHelper.subtract(cfdeps, jdeps);
        log.info("Additional dependencies detected for " + classname + " in " + root + ": " + diff
                + "\ndessert: " + cfdeps
                + "\n  jdeps: " + jdeps);
        Set<String> additionalDependencies = determineDependenciesNotDetectedByJDeps(cf);
        if (!SetHelper.containsAll(additionalDependencies, diff)) {
            log.info("Dump of jdeps-dependencies:\n" + dump(jdeps));
            fail("Dependencies of " + classname + " in " + root + " has unexpected additional dependencies " + SetHelper.subtract(diff, additionalDependencies)
                    + "\ndessert: " + cfdeps
                    + "\n  jdeps: " + jdeps
                    + "\n   diff: " + diff);
        }
    }

    private String dump(Set<String> deps) {
        StringBuilder sb = new StringBuilder();
        for (String cn : deps) {
            sb.append("\"").append(cn).append("\",\n");
        }
        return sb.toString();
    }

    private Set<String> determineDependenciesNotDetectedByJDeps(ClassFile cf) {
        Set<String> referencedClasses = new HashSet<String>();
        determineClassesReferencedByRuntimeAnnotations(referencedClasses, cf);
        determineClassesReferencedBySignatureAttribute(referencedClasses, cf);
        return referencedClasses;
    }

    private void determineClassesReferencedBySignatureAttribute(Set<String> referencedClasses, ClassFile cf) {
        for (AttributeInfo attribute : cf.getAttributes()) {
            if (attribute instanceof SignatureAttribute) {
                attribute.addDependentClassNames(referencedClasses);
            }
        }
    }

    private void determineClassesReferencedByRuntimeAnnotations(Set<String> referencedClasses, ClassFile cf) {
        collectReferencedClasses(referencedClasses, cf.getAttributes());
        for (FieldInfo fieldInfo : cf.getFields()) {
            collectReferencedClasses(referencedClasses, fieldInfo.getAttributes());
        }
        for (MethodInfo methodInfo : cf.getMethods()) {
            collectReferencedClasses(referencedClasses, methodInfo.getAttributes());
        }
    }

    private void collectReferencedClasses(Set<String> referencedClasses, AttributeInfo[] attributes) {
        for (AttributeInfo attribute : attributes) {
            if (attribute instanceof RuntimeVisibleAnnotationsAttribute) {
                collectReferencedClasses(referencedClasses, ((RuntimeVisibleAnnotationsAttribute) attribute).getAnnotations());
            }
            if (attribute instanceof RuntimeVisibleParameterAnnotationsAttribute) {
                for (ParameterAnnotation parameterAnnotation : ((RuntimeVisibleParameterAnnotationsAttribute) attribute).getParameterAnnotations()) {
                    collectReferencedClasses(referencedClasses, parameterAnnotation.getAnnotations());
                }
            }
        }
    }

    private void collectReferencedClasses(Set<String> referencedClasses, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            for (ElementValuePair elementValuePair : annotation.getElementValuePairs()) {
                elementValuePair.addDependentClassNames(referencedClasses);
            }
        }
    }
}

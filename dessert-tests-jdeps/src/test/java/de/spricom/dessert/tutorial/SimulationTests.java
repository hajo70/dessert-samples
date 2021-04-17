package de.spricom.dessert.tutorial;

import de.spricom.dessert.classfile.attribute.AttributeInfo;
import de.spricom.dessert.slicing.*;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.support.AbstractFileItemWriter;

import java.util.SortedMap;
import java.util.TreeMap;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;

public class SimulationTests {
    private static final Classpath cp = new Classpath();

    @Test
    void simulateMakingSpringBatchInfrastructureCycleFree() {
        Root infra = cp.rootOf(ItemReader.class);
        SortedMap<String, PackageSlice> packages = infra.minus(this::isDeprecated).partitionByPackage();
        SortedMap<String, Slice> refactoredPackages = new TreeMap<>(packages);

        simulateMoving(refactoredPackages, ItemStreamException.class, "org.springframework.batch.item.util");
        simulateMoving(refactoredPackages, AbstractFileItemWriter.class, "org.springframework.batch.item.file");

        dessert(refactoredPackages.get("org.springframework.batch.item.support"))
                .usesNot(refactoredPackages.get("org.springframework.batch.item.file"));
        dessert(refactoredPackages).isCycleFree();
    }

    private void simulateMoving(SortedMap<String, Slice> packages, Class<?> type, String targetPackage) {
        String sourcePackage = type.getPackageName();
        Slice clazzSlice = packages.get(sourcePackage).slice(type.getName() + "*"); // Type and inner types
        packages.compute(sourcePackage, (k, v) -> v.minus(clazzSlice).named("refactored slice " + k));
        packages.compute(targetPackage, (k, v) -> v.plus(clazzSlice).named("refactored slice " + k));
    }

    private boolean isDeprecated(Clazz clazz) {
        // using the ClassFile is more efficient than reflection
        for (AttributeInfo attribute : clazz.getClassFile().getAttributes()) {
            if ("Deprecated".equals(attribute.getName())) {
                return true;
            }
        }
        return false;
    }

}

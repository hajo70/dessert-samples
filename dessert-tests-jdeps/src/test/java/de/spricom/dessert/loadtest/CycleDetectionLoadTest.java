package de.spricom.dessert.loadtest;

import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Clazz;
import de.spricom.dessert.slicing.PackageSlice;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;

public class CycleDetectionLoadTest {

    @Test
    public void testPackages() {
        SortedMap<String, PackageSlice> packages = new Classpath().partitionByPackage();
        detectCycles(packages.values(), "packages");
    }

    @Disabled
    @Test
    public void dumpCycleCause() {
        Classpath cp = new Classpath();
        dessert(cp.packageOf("net.bytebuddy.implementation.bytecode.assign.primitive"))
                .usesNot(cp.packageOf("net.bytebuddy.implementation"));
    }

    @Test
    public void testClasses() {
        Set<Clazz> classes = new Classpath().getClazzes();
        detectCycles(classes, "classes");
    }

    private void detectCycles(Collection<? extends Slice> slices, String name) {
        long ts = System.nanoTime();
        try {
            dessert(slices).isCycleFree();
        } catch (AssertionError er) {
            System.out.printf("Cycle detection for %d %s needed %f.2 Sec. and found:%n%s",
                    slices.size(), name,
                    (System.nanoTime() - ts) / 1e9,
                    er.getMessage());
        }
    }
}

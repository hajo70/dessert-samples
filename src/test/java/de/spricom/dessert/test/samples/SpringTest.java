package de.spricom.dessert.test.samples;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.cycles.SingleEntrySlice;
import de.spricom.dessert.cycles.SliceGroup;
import de.spricom.dessert.slicing.*;
import org.fest.assertions.Fail;
import org.junit.Before;
import org.junit.Test;

import de.spricom.dessert.resolve.ClassResolver;
import de.spricom.dessert.util.DependencyGraph;

public class SpringTest {
    private static ClassResolver resolver;

    private SliceContext sc;
    private ConcreteSlice packages;

    @Before
    public void init() throws IOException {
        sc = new SliceContext(getSpringJarsResolver());
        packages = sc.packagesOf(resolver.getRootFiles());
    }

    @Test
    public void testPackageCycles() throws IOException {
        packages = packages.without(sc.subPackagesOf("org.springframework.cglib.core"));
        packages = packages.without(sc.subPackagesOf("org.springframework.objenesis"));
        SliceAssertions.assertThat(SliceGroup.splitByPackage(packages)).isCycleFree();
    }

    @Test
    public void testClassCycles() {
        SliceGroup<SingleEntrySlice> packages = SliceGroup.splitByEntry(sc.packagesOf(resolver.getRootFiles()));
        try {
            SliceAssertions.assertThat(packages).isCycleFree();
            Fail.fail("No cycle found");
        } catch (AssertionError ae) {
            System.out.println(ae.toString());
        }
    }

    @Test
    public void testNestedPackageDependencies() {
        try {
            SliceGroup<de.spricom.dessert.cycles.PackageSlice> group = SliceGroup.splitByPackage(packages);
            for (de.spricom.dessert.cycles.PackageSlice slice : group) {
                SliceAssertions.assertThat(slice).doesNotUse(slice.getParentPackage(group));
            }
            Fail.fail("No dependency found");
        } catch (AssertionError ae) {
            System.out.println(ae.getMessage());
        }
    }

    @Test
    public void testOuterPackageDependencies() {
        try {
            SliceGroup<de.spricom.dessert.cycles.PackageSlice> group = SliceGroup.splitByPackage(packages);
            for (de.spricom.dessert.cycles.PackageSlice slice : group) {
                SliceAssertions.assertThat(slice.getParentPackage(group)).doesNotUse(slice);
            }
            Fail.fail("No dependency found");
        } catch (AssertionError ae) {
            System.out.println(ae.getMessage());
        }
    }

    private static ClassResolver getSpringJarsResolver() throws IOException {
        if (resolver == null) {
            resolver = new ClassResolver();
            for (String filename : System.getProperty("java.class.path").split(File.pathSeparator)) {
                if (filename.contains("spring")) {
                    resolver.add(filename);
                }
            }
        }
        return resolver;
    }
}

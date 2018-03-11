package de.spricom.dessert.test.samples;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.slicing.SingleEntrySlice;
import de.spricom.dessert.slicing.SliceGroup;
import de.spricom.dessert.slicing.*;
import org.fest.assertions.Fail;
import org.junit.Before;
import org.junit.Test;

import de.spricom.dessert.resolve.ClassResolver;

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
        SliceAssertions.assertThat(packages).splitByPackage().isCycleFree();
    }

    @Test
    public void testClassCycles() {
        try {
            SliceAssertions.assertThat(sc.packagesOf(resolver.getRootFiles())).splitByPackage().isCycleFree();
            Fail.fail("No cycle found");
        } catch (AssertionError ae) {
            System.out.println(ae.toString());
        }
    }

    @Test
    public void testNestedPackageDependencies() {
        try {
            SliceGroup<PackageSlice> group = SliceGroup.splitByPackage(packages);
            for (PackageSlice slice : group) {
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
            SliceGroup<PackageSlice> group = SliceGroup.splitByPackage(packages);
            for (PackageSlice slice : group) {
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

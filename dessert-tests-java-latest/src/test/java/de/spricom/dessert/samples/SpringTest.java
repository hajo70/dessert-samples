package de.spricom.dessert.samples;

import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.ConcreteSlice;
import de.spricom.dessert.slicing.PackageSlice;
import de.spricom.dessert.slicing.Slice;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.SortedMap;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;

public class SpringTest {

    private Classpath cp = new Classpath();
    private Slice spring = cp.packageTreeOf("org.springframework..*");

    @Test
    public void checkPackageCycles() {
        SortedMap<String, PackageSlice> packages = spring
                // remove known cycles
                .minus(cp.slice("org.springframework.boot.autoconfigure..*"),
                        cp.slice("org.springframework.boot.cloud.*"),
                        cp.slice("org.springframework.data.jpa.repository.support.*"),
                        cp.slice("org.springframework.cglib.core.*"),
                        cp.slice("org.springframework.objenesis..*"),
                        cp.slice("org.springframework.test..*"))
                .partitionByPackage();
        Assertions.assertThat(packages).hasSizeGreaterThan(10);
        dessert(packages).isCycleFree();
    }

    @Test
    public void showUsageOfNestedPackagesFromOuterPackages() {
        SortedMap<String, PackageSlice> packages = cp.rootOf(ApplicationContext.class).partitionByPackage();
        for (PackageSlice slice : packages.values()) {
            try {
                dessert(slice).usesNot(slice.getParentPackage());
            } catch (AssertionError ae) {
                System.out.println(ae.getMessage());
            }
        }
    }

    @Test
    public void showUsageOfOuterPackagesByNestedPackages() {
        SortedMap<String, PackageSlice> packages = cp.rootOf(ApplicationContext.class).partitionByPackage();
        for (PackageSlice slice : packages.values()) {
            try {
                dessert(slice.getParentPackage()).usesNot(slice);
            } catch (AssertionError ae) {
                System.out.println(ae.getMessage());
            }
        }
    }

    @Test
    void listDuplicates() {
        Classpath cp = new Classpath();
        ConcreteSlice duplicates = cp.duplicates();
        duplicates.getClazzes().forEach(clazz -> System.out.println(clazz.getURI()));
    }

}

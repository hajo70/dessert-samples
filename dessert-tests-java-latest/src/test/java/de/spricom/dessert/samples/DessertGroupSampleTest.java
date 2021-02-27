package de.spricom.dessert.samples;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.PackageSlice;
import de.spricom.dessert.slicing.Root;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Test;

import java.util.SortedMap;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;
import static org.assertj.core.api.Assertions.assertThat;

public class DessertGroupSampleTest {
    private final Classpath cp = new Classpath();
    private final Root dessert = cp.rootOf(Slice.class);

    @Test
    void testPackagesNotEmpty() {
        assertThat(dessert.partitionByPackage()).isNotEmpty();
    }

    @Test
    public void testCycleFree() {
        dessert(dessert.partitionByPackage()).isCycleFree();
    }

    @Test
    public void testNestingRuleNoParentPackage() {
        SortedMap<String, PackageSlice> packages = dessert.partitionByPackage();

        packages.forEach((name, pckg) -> dessert(pckg).usesNot(pckg.getParentPackage()));
    }

    @Test
    public void testNestingRuleNoAncestorPackage() {
        SortedMap<String, PackageSlice> packages = dessert.partitionByPackage();

        packages.forEach((name, pckg) -> SliceAssertions.dessert(pckg)
                .usesNot(dessert.slice(clazz -> pckg.getParentPackageName().startsWith(clazz.getPackageName()))));
    }
}

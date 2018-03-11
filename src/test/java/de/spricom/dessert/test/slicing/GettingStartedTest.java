package de.spricom.dessert.test.slicing;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.slicing.PackageSlice;
import de.spricom.dessert.slicing.SliceGroup;
import de.spricom.dessert.slicing.Slice;
import de.spricom.dessert.slicing.SliceContext;
import org.junit.Test;

import java.io.IOException;

public class GettingStartedTest {

    @Test
    public void checkDessertDependencies() throws IOException {
        SliceContext sc = new SliceContext();
        Slice dessert = sc.subPackagesOf("de.spricom.dessert")
                .without(sc.subPackagesOf("de.spricom.dessert.test"));
        Slice java = sc.subPackagesOf("java");
        SliceAssertions.assertThat(dessert).usesOnly(java);
    }

    @Test
    public void checkPackagesAreCycleFree() throws IOException {
        Slice subPackages = new SliceContext().subPackagesOf("de.spricom.dessert");
        SliceAssertions.dessert(subPackages).splitByPackage().isCycleFree();
    }

    @Test
    public void checkNestedPackagesShouldNotUseOuterPackages() throws IOException {
        SliceGroup<PackageSlice> subPackages = SliceGroup.splitByPackage(new SliceContext().subPackagesOf("de.spricom.dessert"));
        for (PackageSlice pckg : subPackages) {
            SliceAssertions.assertThat(pckg).doesNotUse(pckg.getParentPackage(subPackages));
        }
    }
}

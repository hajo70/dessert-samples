package test.slicing;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.groups.PackageSlice;
import de.spricom.dessert.groups.SliceGroup;
import de.spricom.dessert.slicing.Slice;
import de.spricom.dessert.slicing.SliceContext;
import org.junit.Test;

import java.io.IOException;

public class GettingStartedTest {

    @Test
    public void checkDessertDependencies() throws IOException {
        SliceContext sc = new SliceContext();
        Slice dessert = sc.packageTreeOf("de.spricom.dessert")
                .without(sc.packageTreeOf("de.spricom.dessert.test"));
        Slice java = sc.packageTreeOf("java");
        SliceAssertions.assertThat(dessert).usesOnly(java);
    }

    @Test
    public void checkPackagesAreCycleFree() throws IOException {
        Slice subPackages = new SliceContext().packageTreeOf("de.spricom.dessert");
        SliceAssertions.dessert(subPackages).splitByPackage().isCycleFree();
    }

    @Test
    public void checkNestedPackagesShouldNotUseOuterPackages() throws IOException {
        SliceGroup<PackageSlice> subPackages = SliceGroup.splitByPackage(new SliceContext().packageTreeOf("de.spricom.dessert"));
        for (PackageSlice pckg : subPackages) {
            SliceAssertions.assertThat(pckg).doesNotUse(pckg.getParentPackage(subPackages));
        }
    }
}

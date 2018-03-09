package de.spricom.dessert.test.slicing;

import de.spricom.dessert.slicing.*;
import org.junit.Test;

import java.io.IOException;

public class GettingStartedTest {

    @Test
    public void checkDessertDependencies() throws IOException {
        SliceContext sc = new SliceContext();
        ManifestSliceSet dessert = sc.subPackagesOfManifested("de.spricom.dessert")
                .without(sc.subPackagesOfManifested("de.spricom.dessert.test"));
        SliceSet java = sc.subPackagesOf("java");
        SliceAssertions.assertThat(dessert).usesOnly(java);
    }

    @Test
    public void checkPackagesAreCycleFree() throws IOException {
        ManifestSliceSet subPackages = new SliceContext().subPackagesOfManifested("de.spricom.dessert");
        SliceAssertions.dessert(subPackages).isCycleFree();
    }

    @Test
    public void checkNestedPackagesShouldNotUseOuterPackages() throws IOException {
        ManifestSliceSet subPackages = new SliceContext().subPackagesOfManifested("de.spricom.dessert");
        for (Slice pckg : subPackages) {
            SliceAssertions.assertThat(pckg).doesNotUse(pckg.getParentPackage());
        }
    }
}

package de.spricom.dessert.test.samples;

import java.io.IOException;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.cycles.PackageSlice;
import de.spricom.dessert.cycles.SliceGroup;
import de.spricom.dessert.slicing.*;
import org.junit.Test;

public class DependencyCheck {

    @Test
    public void testPackageDependencies() throws IOException {
        SliceGroup<PackageSlice> subPackages = SliceGroup.splitByPackage(new SliceContext().subPackagesOf("de.spricom.dessert"));
        SliceAssertions.dessert(subPackages).isCycleFree();
        subPackages.forEach(slice -> SliceAssertions.assertThat(slice).doesNotUse(slice.getParentPackage(subPackages)));
    }
}

package de.spricom.dessert.test.samples;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.slicing.PackageSlice;
import de.spricom.dessert.slicing.Slice;
import de.spricom.dessert.slicing.SliceContext;
import de.spricom.dessert.slicing.SliceGroup;
import org.junit.Test;

import java.io.IOException;

public class DependencyCheck {

    @Test
    public void testPackageDependencies() throws IOException {
        Slice slice = new SliceContext().subPackagesOf("de.spricom.dessert");
        SliceGroup<PackageSlice> subPackages = SliceGroup.splitByPackage(slice);
        SliceAssertions.dessert(slice).splitByPackage().isCycleFree();
        subPackages.forEach(pckg -> SliceAssertions.assertThat(pckg).doesNotUse(pckg.getParentPackage(subPackages)));
    }
}

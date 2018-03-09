package de.spricom.dessert.test.samples;

import java.io.IOException;

import de.spricom.dessert.slicing.*;
import org.fest.assertions.Fail;
import org.junit.Test;

import de.spricom.dessert.resolve.ClassResolver;

public class DependencyCheck {

    @Test
    public void testPackageDependencies() throws IOException {
        ManifestSliceSet subPackages = new SliceContext().subPackagesOfManifested("de.spricom.dessert");
        SliceAssertions.dessert(subPackages).isCycleFree();
        
        subPackages.forEach(slice -> SliceAssertions.assertThat(slice).doesNotUse(slice.getParentPackage()));
    }
}

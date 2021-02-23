package test.samples;

import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.PackageSlice;
import de.spricom.dessert.slicing.Slice;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Fail;
import org.junit.jupiter.api.Test;

import java.util.SortedMap;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;

public class SpringTest {

    private Classpath cp = new Classpath();
    private Slice spring = cp.packageTreeOf("org.springframework..*");

    @Test
    public void testPackageCycles() {
        SortedMap<String, PackageSlice> packages = spring
                .minus(cp.packageOf("org.springframework.cglib.core"),
                        cp.packageOf("org.springframework.objenesis"),
                        cp.packageOf("org.springframework.test.web.servlet.request"))
                .partitionByPackage();
        Assertions.assertThat(packages).hasSizeGreaterThan(10);
        dessert(packages).isCycleFree();
    }

    @Test
    public void testNestedPackageDependencies() {
        try {
            SortedMap<String, PackageSlice> packages = spring.partitionByPackage();
            for (PackageSlice slice : packages.values()) {
                dessert(slice).usesNot(slice.getParentPackage());
            }
            Fail.fail("No dependency found");
        } catch (AssertionError ae) {
            System.out.println(ae.getMessage());
        }
    }

    @Test
    public void testOuterPackageDependencies() {
        try {
            SortedMap<String, PackageSlice> packages = spring.partitionByPackage();
            for (PackageSlice slice : packages.values()) {
                dessert(slice.getParentPackage()).usesNot(slice);
            }
            Fail.fail("No dependency found");
        } catch (AssertionError ae) {
            System.out.println(ae.getMessage());
        }
    }
}

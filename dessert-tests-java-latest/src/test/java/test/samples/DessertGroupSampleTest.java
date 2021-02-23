package test.samples;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.PackageSlice;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Test;

import java.util.SortedMap;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;
import static org.assertj.core.api.Assertions.assertThat;

public class DessertGroupSampleTest {

    @Test
    public void testCycleFree() {
        Slice slice = new Classpath().packageTreeOf("de.spricom.dessert");

        dessert(slice.partitionByPackage()).isCycleFree();
    }

    @Test
    public void testNestingRuleNoParentPackage() {
        Slice slice = new Classpath().packageTreeOf("de.spricom.dessert");
        SortedMap<String, PackageSlice> packages = slice.partitionByPackage();

        packages.forEach((name, pckg) -> dessert(pckg).usesNot(pckg.getParentPackage()));
    }

    @Test
    public void testNestingRuleNoAncestorPackage() {
        Slice slice = new Classpath().packageTreeOf("de.spricom.dessert");
        SortedMap<String, PackageSlice> packages = slice.partitionByPackage();

        packages.forEach((name, pckg) -> SliceAssertions.assertThat(pckg)
                .usesNot(slice.slice(clazz -> pckg.getParentPackageName().startsWith(clazz.getPackageName()))));
    }

    @Test
    public void testNoDuplicates() {
        Classpath cp = new Classpath();
        Slice duplicates = cp.duplicates().minus(cp.asClazz("module-info"));
        StringBuilder sb = new StringBuilder();
        duplicates.getClazzes().forEach(entry -> sb.append(entry.getURI()).append("\n"));
        assertThat(duplicates.getClazzes()).as(sb.toString()).isEmpty();
    }
}

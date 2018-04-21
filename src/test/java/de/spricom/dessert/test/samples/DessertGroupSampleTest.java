package de.spricom.dessert.test.samples;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.groups.PackageSlice;
import de.spricom.dessert.groups.SliceGroup;
import de.spricom.dessert.slicing.ConcreteSlice;
import de.spricom.dessert.slicing.Slice;
import de.spricom.dessert.slicing.SliceContext;
import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class DessertGroupSampleTest {

    @Test
    public void testCycleFree() {
        Slice slice = new SliceContext().packageTreeOf("de.spricom.dessert");

        SliceGroup<PackageSlice> sg = SliceGroup.splitByPackage(slice);
        SliceAssertions.dessert(sg).isCycleFree();

        // short form:
        SliceAssertions.dessert(slice).splitByPackage().isCycleFree();
    }

    @Test
    public void testNestingRuleNoParentPackage() {
        Slice slice = new SliceContext().packageTreeOf("de.spricom.dessert");
        SliceGroup<PackageSlice> packages = SliceGroup.splitByPackage(slice);

        packages.forEach(pckg -> SliceAssertions.assertThat(pckg)
                .doesNotUse(pckg.getParentPackage(packages)));

    }

    @Test
    public void testNestingRuleNoAncestorPackage() {
        Slice slice = new SliceContext().packageTreeOf("de.spricom.dessert");
        SliceGroup<PackageSlice> packages = SliceGroup.splitByPackage(slice);

        packages.forEach(pckg -> SliceAssertions.assertThat(pckg)
                .doesNotUse(slice.slice(entry -> pckg.getParentPackageName().startsWith(entry.getPackageName()))));
    }

    @Ignore
    @Test
    public void testNoDuplicates() {
        ConcreteSlice duplicates = new SliceContext().duplicates();
        StringBuilder sb = new StringBuilder();
        duplicates.getSliceEntries().forEach(entry -> sb.append(entry.getURI()).append("\n"));
        assertThat(duplicates.getSliceEntries()).as(sb.toString()).isEmpty();
    }
}

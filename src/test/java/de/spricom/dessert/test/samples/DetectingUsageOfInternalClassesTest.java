package de.spricom.dessert.test.samples;

import de.spricom.dessert.slicing.Slice;
import de.spricom.dessert.slicing.SliceContext;
import org.junit.Test;

import static de.spricom.dessert.assertions.SliceAssertions.assertThat;

public class DetectingUsageOfInternalClassesTest {

    private void use() {
        // Integer.class.equals(Base64Data.class);
    }

    @Test
    public void testDoesNotUseInternalClasses() {
        SliceContext sc = new SliceContext();
        Slice dessert = sc.packageTreeOf("de.spricom.dessert");

        assertThat(dessert).doesNotUse(
                sc.packageTreeOf("com.sun"),
                sc.packageTreeOf("sun"));

        if (false)
            assertThat(dessert).usesOnly(sc.packageTreeOf("java"));
    }
}

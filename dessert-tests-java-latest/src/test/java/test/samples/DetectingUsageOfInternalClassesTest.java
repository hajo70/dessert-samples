package test.samples;

import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Test;

import static de.spricom.dessert.assertions.SliceAssertions.assertThat;

public class DetectingUsageOfInternalClassesTest {

    private void use() {
        // Integer.class.equals(Base64Data.class);
    }

    @Test
    public void testDoesNotUseInternalClasses() {
        Classpath sc = new Classpath();
        Slice dessert = sc.packageTreeOf("de.spricom.dessert");

        assertThat(dessert).usesNot(
                sc.packageTreeOf("com.sun"),
                sc.packageTreeOf("sun"));

        assertThat(dessert).usesOnly(sc.packageTreeOf("java"));
    }
}

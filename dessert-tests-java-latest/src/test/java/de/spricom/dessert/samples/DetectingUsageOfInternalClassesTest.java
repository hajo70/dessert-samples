package de.spricom.dessert.samples;

import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Clazz;
import org.junit.jupiter.api.Test;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;
import static org.assertj.core.api.Assertions.assertThat;

public class DetectingUsageOfInternalClassesTest {

    private void use() {
        Integer.class.equals(Base64Data.class);
    }

    @Test
    public void testDoesNotUseInternalClasses() {
        Classpath cp = new Classpath();
        Clazz me = cp.asClazz(this.getClass());

        try {
            dessert(me).usesNot(cp.slice("..sun..*"));
            throw new IllegalStateException("Usage of internal class not detected."); // Cannot throw AssertionError here
        } catch (AssertionError er) {
            System.out.println(er.getMessage());
            assertThat(er.getMessage().trim()).isEqualToNormalizingWhitespace("Illegal Dependencies:\n" +
                    "de.spricom.dessert.samples.DetectingUsageOfInternalClassesTest\n" +
                    " -> com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data\n");
        }
    }
}

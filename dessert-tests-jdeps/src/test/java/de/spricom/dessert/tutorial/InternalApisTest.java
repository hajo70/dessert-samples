package de.spricom.dessert.tutorial;

import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.ObjectContentAssert;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;

public class InternalApisTest {
    private static final Classpath cp = new Classpath();

    @Disabled("will fail")
    @Test
    @DisplayName("Detect usage of internal APIs")
    void detectSpringInternalApis() {
        Slice springframework = cp.slice("org.springframework..*");
        dessert(springframework).usesNot(
                cp.slice("com.sun..*"),
                cp.slice("sun..*"),
                cp.slice("..internal..*").minus(springframework)
        );
    }

    @Test
    @DisplayName("Make sure springframework adds no internal API usages")
    void detectSpringAddionalInternalApis() {
        Slice springframework = cp.slice("org.springframework..*")
                .minus(cp.sliceOf(ObjectContentAssert.class))
                .minus(cp.slice(SpringJUnit4ClassRunner.class.getName() + "*"))
                .minus(cp.slice("org.springframework.objenesis.instantiator.sun|util.*"));
        dessert(springframework).usesNot(
                cp.slice("com.sun..*"),
                cp.slice("sun..*"),
                cp.slice("..internal..*").minus(springframework)
        );
    }
}

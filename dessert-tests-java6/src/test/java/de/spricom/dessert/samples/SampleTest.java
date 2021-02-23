package de.spricom.dessert.samples;

import de.spricom.dessert.slicing.Classpath;
import org.junit.Test;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class SampleTest {
    private Classpath cp = new Classpath();

    @Test
    public void detectCycle() {
        try {
            dessert(cp.rootOf(Foo.class).getClazzes()).isCycleFree();
            fail("No AssertionError");
        } catch (AssertionError er) {
            assertThat(er.getMessage().trim()).isEqualTo("Cycle:\n" +
                    "clazz de.spricom.dessert.samples.Bar,\n" +
                    "clazz de.spricom.dessert.samples.Baz,\n" +
                    "clazz de.spricom.dessert.samples.Foo,\n" +
                    "clazz de.spricom.dessert.samples.Bar");
        }
    }

    @Test
    public void checkDependencies() {
        dessert(cp.rootOf(Foo.class).packageOf(Foo.class))
                .usesOnly(cp.slice("java.lang|io..*"));
    }

    @Test
    public void checkWithTestDependencies() {
        try {
            dessert(cp.packageOf(Foo.class))
                    .usesOnly(cp.slice("java.lang|io..*"));
            fail("No AssertionError");
        } catch (AssertionError er) {
            assertThat(er.getMessage().trim()).isEqualTo("Illegal Dependencies:\n" +
                    "de.spricom.dessert.samples.SampleTest\n" +
                    " -> de.spricom.dessert.assertions.SliceAssert\n" +
                    " -> de.spricom.dessert.assertions.SliceAssertions\n" +
                    " -> de.spricom.dessert.slicing.Classpath\n" +
                    " -> de.spricom.dessert.slicing.Root\n" +
                    " -> de.spricom.dessert.slicing.Slice\n" +
                    " -> java.util.Set\n" +
                    " -> org.fest.assertions.Assertions\n" +
                    " -> org.fest.assertions.Fail\n" +
                    " -> org.fest.assertions.StringAssert\n" +
                    " -> org.junit.Test");
        }
    }

    @Test
    public void checkIllegalDependency() {
        try {
            dessert(cp.rootOf(Foo.class).packageOf(Foo.class))
                    .usesNot(cp.slice("java.io..*"));
            fail("No AssertionError");
        } catch (AssertionError er) {
            assertThat(er.getMessage().trim()).isEqualTo("Illegal Dependencies:\n" +
                    "de.spricom.dessert.samples.Baz\n" +
                    " -> java.io.PrintStream");
        }
    }
}
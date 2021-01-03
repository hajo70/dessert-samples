package de.spricom.dessert.jdeps;

import de.spricom.dessert.classfile.ClassFile;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@Disabled
public class InvestigateSpecialCasesTest {

    /**
     * {@link AbstractObjectAssert} has a dependency to {@link org.assertj.core.api.Assert},
     * because it uses the {@link org.assertj.core.api.AssertFactory}. This is a method-type
     * that returns some generic extension of {@link org.assertj.core.api.Assert}.
     * Jdeps does not consider this to be a dependency.
     */
    @Test
    void testAbstractObjectAssert() throws IOException {
        ClassFile cf = new ClassFile(AbstractObjectAssert.class);
        cf.getDependentClasses().forEach(System.out::println);
        System.out.println(cf.dumpConstantPool());
    }
}

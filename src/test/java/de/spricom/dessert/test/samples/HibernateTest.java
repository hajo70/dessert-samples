package de.spricom.dessert.test.samples;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.slicing.SingleEntrySlice;
import de.spricom.dessert.slicing.SliceGroup;
import de.spricom.dessert.resolve.ClassResolver;
import de.spricom.dessert.slicing.*;
import org.fest.assertions.Fail;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

public class HibernateTest {
    private static ClassResolver resolver;

    private SliceContext sc;

    @Before
    public void init() throws IOException {
        sc = new SliceContext(getHibernateJarsResolver());
    }

    @Test
    public void testCycleFree() throws IOException {
        try {
            SliceAssertions.assertThat(sc.packagesOf(resolver.getRootFiles())).splitByPackage().isCycleFree();
            Fail.fail("No cycle found");
        } catch (AssertionError ae) {
            System.out.println(ae.toString());
        }
    }

    @Test
    public void testClasses() {
        try {
            SliceAssertions.assertThat(sc.packagesOf(resolver.getRootFiles())).splitByPackage().isCycleFree();
            Fail.fail("No cycle found");
        } catch (AssertionError ae) {
            System.out.println(ae.toString());
        }
    }

    private static ClassResolver getHibernateJarsResolver() throws IOException {
        if (resolver == null) {
            resolver = new ClassResolver();
            for (String filename : System.getProperty("java.class.path").split(File.pathSeparator)) {
                if (filename.contains("hibernate")) {
                    resolver.add(filename);
                }
            }
        }
        return resolver;
    }
}

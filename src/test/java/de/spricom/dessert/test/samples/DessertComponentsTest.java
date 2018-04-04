package de.spricom.dessert.test.samples;

import de.spricom.dessert.slicing.Slice;
import de.spricom.dessert.slicing.SliceContext;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Collection;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;

public class DessertComponentsTest {

    @Test
    public void testDessertComponents() {
        SliceContext sc = new SliceContext();

        Slice java = sc.packageTreeOf(String.class)
                .with(sc.packageTreeOf(URL.class))
                .with(sc.packageTreeOf(File.class))
                .with(sc.packageTreeOf(Collection.class));

        String prefix = "de.spricom.dessert";
        Slice assertions = sc.packageTreeOf(prefix + ".assertions");
        Slice groups = sc.packageTreeOf(prefix + ".groups");
        Slice slicing = sc.packageTreeOf(prefix + ".slicing");
        Slice resolve = sc.packageTreeOf(prefix + ".resolve");
        Slice util = sc.packageTreeOf(prefix + ".util");
        Slice classfile = sc.packageTreeOf(prefix + ".classfile");

        dessert(assertions).usesOnly(groups, slicing, util, java);
        dessert(groups).usesOnly(slicing, java);
        dessert(slicing).usesOnly(resolve, util, classfile, java);
        dessert(resolve).usesOnly(util, classfile, java);
        dessert(classfile).usesOnly(java);
    }
}

package de.spricom.dessert.util;

import de.spricom.dessert.jdeps.JdepsWrapper;
import de.spricom.dessert.slicing.SliceEntry;

import java.io.File;

public final class LookupUtil {

    private LookupUtil() {
    }

    public static File getRootFile(Class<?> clazz) {
        return SliceEntry.getRootFile(JdepsWrapper.class);
    }
}

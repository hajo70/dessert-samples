package test.samples;

import de.spricom.dessert.slicing.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;
import static org.assertj.core.api.Assertions.assertThat;


public class HibernateTest {
    private final Classpath cp = new Classpath();
    private Slice hibernate;

    @BeforeEach
    public void init() throws IOException {
        List<Slice> hibernateRoots =
                Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                        .filter(name -> name.contains("hibernate"))
                        .peek(System.out::println)
                        .map(name -> cp.rootOf(new File(name)))
                        .collect(Collectors.toList());
        hibernate = Slices.of(hibernateRoots);
    }

    @Test
    public void testCycleFree() throws IOException {
        SortedMap<String, PackageSlice> packages = hibernate.partitionByPackage();
        assertThat(packages).hasSizeGreaterThan(10);
        dessert(packages).isCycleFree();
    }

    @Test
    public void testClasses() {
        Set<Clazz> clazzes = hibernate.getClazzes();
        assertThat(clazzes).hasSizeGreaterThan(1000);
        dessert(clazzes).isCycleFree();
    }
}

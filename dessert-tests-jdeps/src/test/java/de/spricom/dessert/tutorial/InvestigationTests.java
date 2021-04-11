package de.spricom.dessert.tutorial;

import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Clazz;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

public class InvestigationTests {
    private static final Classpath cp = new Classpath();

    @Test
    void showReflectionUsageForSpringFramework() {
        Slice spring = cp.slice("org.springframework..*");
        spring.slice(c -> c.uses(cp.slice("java.lang.reflect..*")))
                .getClazzes().forEach(System.out::println);
    }

    @Test
    void showReflectionUsageForSpringFrameworkWithDetails() {
        Slice spring = cp.slice("org.springframework..*");
        Slice reflection = cp.slice("java.lang.reflect..*");
        spring.slice(c -> c.uses(reflection)).getClazzes().stream().sorted().forEach(c ->
                System.out.printf("%s[%s]%n", c.getName(),
                        c.getDependencies().slice(reflection).getClazzes().stream()
                                .map(Clazz::getSimpleName)
                                .collect(Collectors.joining(", "))));
    }

}

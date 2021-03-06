package de.spricom.dessert.loadtest;

import de.spricom.dessert.slicing.*;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;
import static org.assertj.core.api.Assertions.assertThat;

public class ClasspathLoadTest {
    private static final Classpath cp = new Classpath();

    @Test
    void detectDuplicatesFilterByMavenArtifacts() {
        ConcreteSlice duplicates = cp.duplicates();
        List<URI> jars = duplicates.minus("module-info").getClazzes().stream()
                .map(c -> cp.rootOf(c.getRootFile()))
                .map(Root::getURI)
                .distinct()
                .filter(uri -> !uri.toString().contains("/jakarta/activation/jakarta.activation-api/"))
                .filter(uri -> !uri.toString().contains("/com/sun/activation/jakarta.activation/"))
                .filter(uri -> !uri.toString().contains("/jakarta/annotation/jakarta.annotation-api/"))
                .filter(uri -> !uri.toString().contains("/javax/annotation/javax.annotation-api/"))
                .filter(uri -> !uri.toString().contains("/jakarta/validation/jakarta.validation-api/"))
                .filter(uri -> !uri.toString().contains("/javax/validation/validation-api/"))
                .sorted()
                .collect(Collectors.toList());
        assertThat(jars).isEmpty();
    }

    @Test
    void detectDuplicatesFilterByPackage() {
        Slice duplicates = cp.duplicates()
                .minus("module-info")
                .minus("javax.activation.*")
                .minus("javax.annotation..*")
                .minus("javax.validation..*");
        Stream<URI> duplicateURIs = duplicates.getClazzes().stream()
                .sorted(Comparator.comparing(Clazz::getName).thenComparing(Clazz::getURI))
                .map(Clazz::getURI);
        assertThat(duplicateURIs).isEmpty();
    }

    @Test
    void bigFailure() {
        try {
            dessert(cp).usesNot(cp.slice("java.lang..*"));
            throw new IllegalStateException("assertion didn't fail");
        } catch (AssertionError er) {
            System.out.println("Message length: " + er.getMessage().length());
            assertThat(er.getMessage()).hasSizeGreaterThan(5_000_000);
        }
    }
}

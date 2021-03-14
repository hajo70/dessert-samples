package de.spricom.dessert.tutorial;

import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Clazz;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.HamcrestCondition.matching;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;

public class DuplicatesTest {
    private static final Classpath cp = new Classpath();

    @Disabled("will fail")
    @Test
    @DisplayName("Detect duplicates")
    void detectDuplicates() {
        Slice duplicates = cp.duplicates().minus("module-info");

        List<File> duplicateJars = duplicates.getClazzes().stream()
                .map(Clazz::getRootFile).distinct()
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());

        Map<String, Set<File>> duplicateJarsByClass = duplicates.getClazzes().stream()
                .collect(Collectors.groupingBy(Clazz::getName,
                        TreeMap::new,
                        Collectors.mapping(Clazz::getRootFile, Collectors.toSet())));

        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            pw.printf("%nThere are %d duplicate classes spread over %d jars:%n",
                    duplicateJarsByClass.size(), duplicateJars.size());
            pw.println("\nDuplicate classes:");
            duplicateJarsByClass.forEach((name, files) -> pw.printf("%s (%s)%n", name,
                    files.stream().map(File::getName).sorted().collect(Collectors.joining(", "))));
            pw.println("\nJARs containing duplicates:");
            duplicateJars.forEach(jar -> pw.printf("%s%n", jar.getName()));
        }

        assertThat(duplicates.getClazzes().size()).as(sw.toString()).isEqualTo(0);
    }

    @Test
    @DisplayName("Make sure there are no additional duplicates")
    void ensureNoAdditonalDuplicates() {
        Slice duplicates = cp.duplicates().minus("module-info");

        List<File> duplicateJars = duplicates.getClazzes().stream()
                .map(Clazz::getRootFile).distinct()
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());

        Map<String, Set<File>> duplicateJarsByClass = duplicates.getClazzes().stream()
                .collect(Collectors.groupingBy(Clazz::getName,
                        TreeMap::new,
                        Collectors.mapping(Clazz::getRootFile, Collectors.toSet())));

        System.out.printf("There are %d duplicate classes spread over %d jars:%n",
                duplicateJarsByClass.size(), duplicateJars.size());
        System.out.println("\nDuplicate classes:");
        duplicateJarsByClass.forEach((name, files) -> System.out.printf("%s (%s)%n", name,
                files.stream().map(File::getName).sorted().collect(Collectors.joining(", "))));
        System.out.println("\nJARs containing duplicates:");
        duplicateJars.forEach(jar -> System.out.printf("%s%n", jar.getName()));

        // make sure there are no additional jars involved
        assertThat(duplicateJars.stream().map(File::getName))
                .areAtLeast(2, matching(startsWith("jakarta.activation")))
                .areAtLeast(2, matching(containsString("annotation-api")))
                .areAtLeast(2, matching(containsString("validation-api")))
                .hasSize(6);

        // make sure there are no additonal classes involved
        assertThat(duplicates
                .minus("javax.activation|validation|annotation..*")
                .getClazzes()).isEmpty();
    }
}

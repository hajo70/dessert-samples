package de.spricom.dessert.tutorial;

import de.spricom.dessert.classfile.attribute.AttributeInfo;
import de.spricom.dessert.slicing.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemReader;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;
import static org.assertj.core.api.Assertions.assertThat;

public class CyclesTest {
    private static final Classpath cp = new Classpath();

    @Test
    void detectReactorCycles() {
        Root root = cp.rootOf(Flux.class);
        Map<String, PackageSlice> packages = root.partitionByPackage();
        Map<String, Slice> modules = new TreeMap<>();

        modules.put("reactor.cycle1", Slices.of(
                packages.remove("reactor.core.publisher"),
                packages.remove("reactor.adapter"),
                packages.remove("reactor.util.concurrent"),
                packages.remove("reactor.util.retry")
        ).named("reactor.core cycle 1"));
        modules.putAll(packages);

        dessert(modules).isCycleFree();
    }

    @Test
    void investigateReactorCycle() {
        Root root = cp.rootOf(Flux.class);
        Map<String, PackageSlice> packages = root.partitionByPackage();

        PackageSlice publisher = packages.get("reactor.core.publisher");
        PackageSlice adapter = packages.get("reactor.adapter");
        PackageSlice utilConcurrent = packages.get("reactor.util.concurrent");
        PackageSlice utilRetry = packages.get("reactor.util.retry");

        investigateCycle(List.of(publisher, adapter, utilConcurrent, utilRetry));
    }

    @Test
    void investigateSpringFrameworkPackageCycles() {
        SortedMap<String, PackageSlice> packages = cp.slice("org.springframework..*").partitionByPackage();

        List<Slice> cycle1 = List.of(packages.remove("org.springframework.cglib.core"),
                packages.remove("org.springframework.cglib.core.internal"),
                packages.remove("org.springframework.cglib.transform"));

        List<Slice> cycle2 = List.of(packages.remove("org.springframework.objenesis"),
                packages.remove("org.springframework.objenesis.strategy"),
                packages.remove("org.springframework.objenesis.instantiator.basic"),
                packages.remove("org.springframework.objenesis.instantiator.util"),
                packages.remove("org.springframework.objenesis.instantiator.sun"),
                packages.remove("org.springframework.objenesis.instantiator.gcj"),
                packages.remove("org.springframework.objenesis.instantiator.perc"),
                packages.remove("org.springframework.objenesis.instantiator.android"));

        List<Slice> cycle3 = List.of(packages.remove("org.springframework.boot.cloud"),
                packages.remove("org.springframework.boot.context.config"));

        List<Slice> cycle4 = List.of(packages.remove("org.springframework.test.web.servlet"),
                packages.remove("org.springframework.test.web.servlet.request"),
                packages.remove("org.springframework.test.web.servlet.result"));

        List<Slice> cycle5 = List.of(packages.remove("org.springframework.boot.autoconfigure.orm.jpa"),
                packages.remove("org.springframework.boot.autoconfigure.data.neo4j"),
                packages.remove("org.springframework.boot.autoconfigure.jdbc"),
                packages.remove("org.springframework.boot.autoconfigure.transaction"),
                packages.remove("org.springframework.boot.autoconfigure.transaction.jta"));

        List<Slice> cycle6 = List.of(packages.remove("org.springframework.security.config.method"),
                packages.remove("org.springframework.security.config"),
                packages.remove("org.springframework.security.config.http"),
                packages.remove("org.springframework.security.config.ldap"),
                packages.remove("org.springframework.security.config.websocket"),
                packages.remove("org.springframework.security.config.authentication"),
                packages.remove("org.springframework.security.config.debug"));

        List<Slice> cycle7 = List.of(packages.remove("org.springframework.batch.item"),
                packages.remove("org.springframework.batch.item.util"));

        List<Slice> cycle8 = List.of(packages.remove("org.springframework.batch.core"),
                packages.remove("org.springframework.batch.core.scope.context"),
                packages.remove("org.springframework.batch.core.repository"),
                packages.remove("org.springframework.batch.core.launch"),
                packages.remove("org.springframework.batch.core.explore"));

        List<Slice> cycle9 = List.of(packages.remove("org.springframework.batch.item.file"),
                packages.remove("org.springframework.batch.item.support"));

        List<Slice> cycle10 = List.of(packages.remove("org.springframework.batch.core.launch.support"),
                packages.remove("org.springframework.batch.core.step.tasklet"),
                packages.remove("org.springframework.batch.core.configuration"),
                packages.remove("org.springframework.batch.core.configuration.support"),
                packages.remove("org.springframework.batch.core.step"));

        List<Slice> cycle11 = List.of(packages.remove("org.springframework.batch.core.jsr.step"),
                packages.remove("org.springframework.batch.core.jsr.job.flow"),
                packages.remove("org.springframework.batch.core.jsr.partition.support"),
                packages.remove("org.springframework.batch.core.jsr.configuration.xml"),
                packages.remove("org.springframework.batch.core.jsr.step.builder"));

        List<Slice> cycle12 = List.of(packages.remove("org.springframework.batch.core.jsr.job.flow.support"),
                packages.remove("org.springframework.batch.core.jsr.job.flow.support.state"));

        Map<String, Slice> mergedPackages = new HashMap<>(packages);
        int i = 0;
        for (List<Slice> cycle : List.of(cycle1, cycle2, cycle3, cycle4, cycle5, cycle6, cycle7, cycle8, cycle9,
                cycle10, cycle11, cycle12)) {
            i++;
            System.out.printf("%n----- CYCLE %d ------------------------------------------------%n", i);
            investigateCycle(cycle);
            mergedPackages.put("cycle" + i, Slices.of(cycle).named("cycle" + i));
        }

        dessert(mergedPackages).isCycleFree();
    }

    @Test
    void investigateSpringBatchInfrastructureCycles() {
        SortedMap<String, PackageSlice> packages = cp.rootOf(ItemReader.class)
                .minus(this::isDeprecated)
                .partitionByPackage();
        Map<String, Slice> mergedPackages = new HashMap<>(packages);

        List<Stream<String>> cycles = List.of(
                Stream.of("item", "item.util"),
                Stream.of("item.file", "item.support")
        );

        int i = 0;
        for (Stream<String> involvedPackages : cycles) {
            List<Slice> cycle = involvedPackages
                    .map("org.springframework.batch."::concat)
                    .map(mergedPackages::remove).collect(Collectors.toList());
            i++;
            System.out.printf("%n----- CYCLE %d ------------------------------------------------%n", i);
            investigateCycle(cycle);
            mergedPackages.put("cycle" + i, Slices.of(cycle).named("cycle" + i));
        }

        dessert(mergedPackages).isCycleFree();
    }

    private void investigateCycle(List<Slice> slices) {
        investigateCycle(slices, c -> c.getName().substring(c.getPackageName().length() + 1));
    }

    private void investigateCycle(List<Slice> slices, Function<Clazz, String> name) {
        permute(slices).forEach(p -> {
            Slice l = p.getLeft();
            Slice r = p.getRight();
            if (l.uses(r)) {
                System.out.printf("\n%s -> %s:%n", p.getLeft(), p.getRight());
                for (Clazz clazz : l.slice(c -> c.uses(r)).getClazzes()) {
                    String usages = clazz.getDependencies().slice(r).getClazzes().stream()
                            .map(name).collect(Collectors.joining(", "));
                    System.out.printf("  %s uses %s%n", name.apply(clazz), usages);
                }
            }
        });
    }

    @Test
    void checkJUnit5IsCycleFree() {
        Root junit4 = cp.rootOf(Before.class);
        Slice junit5 = cp.slice("org.junit..*")
                .minus(junit4) // ignore old junit4 classes
                .minus("..shadow..*") // shadow packages don't belong to junit itself
                .minus(this::isDeprecated); // ignore deprecated classes
        dessert(junit5.partitionByPackage()).isCycleFree();
    }

    private boolean isDeprecated(Clazz clazz) {
        // using the ClassFile is more efficient than reflection
        for (AttributeInfo attribute : clazz.getClassFile().getAttributes()) {
            if ("Deprecated".equals(attribute.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isDeprecatedUsingReflection(Clazz clazz) {
        try {
            return clazz.getClassImpl().getAnnotation(Deprecated.class) != null;
        } catch (NoClassDefFoundError er) {
            // ignore some Kotlin classes that can't be loaded through reflection
            return false;
        }
    }

    @Test
    void testPairs() {
        int n = 5;
        List<Integer> ints = IntStream.rangeClosed(1, n).boxed().collect(Collectors.toList());
        List<Pair<Integer, Integer>> perms = pairs(ints).collect(Collectors.toList());
        for (var p : perms) {
            System.out.printf("(%d,%d)%n", p.getLeft(), p.getRight());
        }
        int sz = 1 + 2 + 3 + 4;
        assertThat(perms).hasSize(sz);
        assertThat(perms.stream().map(p -> p.getLeft() + "/" + p.getRight()).collect(Collectors.toSet())).hasSize(sz);
    }

    private <X> Stream<Pair<X, X>> permute(List<X> list) {
        return pairs(list).flatMap(p -> Stream.of(p, Pair.of(p.getRight(), p.getLeft())));
    }

    private <X> Stream<Pair<X, X>> pairs(List<X> list) {
        int sz = list.size();
        if (sz < 2) {
            throw new IllegalArgumentException("sz = " + sz);
        }
        if (sz == 2) {
            return Stream.of(Pair.of(list.get(0), list.get(1)));
        }
        X first = list.get(0);
        Stream<Pair<X, X>> pairs = IntStream.range(1, sz)
                .mapToObj(list::get)
                .map(r -> Pair.of(first, r));
        return Stream.concat(pairs, pairs(list.subList(1, sz)));
    }
}

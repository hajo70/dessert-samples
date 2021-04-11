package de.spricom.dessert.tutorial;

import de.spricom.dessert.slicing.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;

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
        PackageSlice utilCuncurrent = packages.get("reactor.util.concurrent");
        PackageSlice utilRetry = packages.get("reactor.util.retry");

        investigateCycle(List.of(publisher, adapter, utilCuncurrent, utilRetry));
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
    void investigateJUnit5PackageCycles() {
        SortedMap<String, PackageSlice> packages = cp.slice("org.junit..*").partitionByPackage();
        Map<String, Slice> mergedPackages = new HashMap<>(packages);

        List<List<Slice>> cycles = List.of(
                Stream.of("runner", "runner.notification", "runner.manipulation", "runners.model",
                        "validator", "experimental",
                        "internal", "internal.requests", "internal.runners", "internal.builders")
                        .map("org.junit."::concat)
                        .map(mergedPackages::remove).collect(Collectors.toList()),
                Stream.of("", ".util", ".function", ".logging")
                        .map("org.junit.platform.commons"::concat)
                        .map(mergedPackages::remove).collect(Collectors.toList()),
                Stream.of("", ".core")
                        .map("org.junit.jupiter.params.shadow.com.univocity.parsers.common.processor"::concat)
                        .map(mergedPackages::remove).collect(Collectors.toList()),
                Stream.of("fixed", "common", "common.input", "common.iterators", "common.routine",
                        "common.fields", "common.record")
                        .map("org.junit.jupiter.params.shadow.com.univocity.parsers."::concat)
                        .map(mergedPackages::remove).collect(Collectors.toList()),
                Stream.of("conversions", "annotations", "annotations.helpers")
                        .map("org.junit.jupiter.params.shadow.com.univocity.parsers."::concat)
                        .map(mergedPackages::remove).collect(Collectors.toList()),
                Stream.of("", ".rules", ".internal.runners.rules", ".runners", ".runners.parameterized")
                        .map("org.junit"::concat)
                        .map(mergedPackages::remove).collect(Collectors.toList()),
                Stream.of("", ".internal")
                        .map("org.junit.experimental.theories"::concat)
                        .map(mergedPackages::remove).collect(Collectors.toList())
        );

        int i = 1;
        // cycle1 ist to big to show all permutations
        for (List<Slice> cycle : cycles.subList(1, cycles.size())) {
            i++;
            System.out.printf("%n----- CYCLE %d ------------------------------------------------%n", i);
            investigateCycle(cycle);
            mergedPackages.put("cycle" + i, Slices.of(cycle).named("cycle" + i));
        }

        cycles = List.of(
                Stream.of("cycle2", "cycle3", "cycle4", "cycle5",
                        "org.junit.jupiter.params.shadow.com.univocity.parsers.common.input.concurrent")
                        .map(mergedPackages::remove).collect(Collectors.toList()));

        i = 0;
        for (List<Slice> cycle : cycles) {
            i++;
            System.out.printf("%n----- BIG CYCLE %d --------------------------------------------%n", i);
            investigateCycle(cycle, Clazz::getName);
            mergedPackages.put("big-cycle" + i, Slices.of(cycle).named("big-cycle" + i));
        }

        dessert(mergedPackages).isCycleFree();
    }

    private void investigateCycle(List<Slice> slices) {
        investigateCycle(slices, c -> c.getName().substring(c.getPackageName().length() + 1));
    }

    private void investigateCycle(List<Slice> slices, Function<Clazz, String> name) {
        for (var p : permute(slices)) {
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
        }
    }

    @Test
    void testPermute() {
        permute(IntStream.rangeClosed(1, 4).boxed().collect(Collectors.toList())).forEach(System.out::println);
    }

    private <X> List<Pair<X, X>> permute(List<X> list) {
        int sz = list.size();
        if (sz < 2) {
            throw new IllegalArgumentException("sz = " + sz);
        }
        if (sz == 2) {
            return List.of(Pair.of(list.get(0), list.get(1)), Pair.of(list.get(1), list.get(0)));
        }
        List<Pair<X, X>> result = new LinkedList<>();
        for (int i = 0; i < sz; i++) {
            result.addAll(permute(
                    Stream.concat(
                            list.subList(0, i).stream(),
                            list.subList(i + 1, sz).stream())
                            .collect(Collectors.toList())));
        }
        return result;
    }

}

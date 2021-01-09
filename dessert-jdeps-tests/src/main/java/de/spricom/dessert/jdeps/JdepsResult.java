package de.spricom.dessert.jdeps;

import java.util.*;

public class JdepsResult {
    private final Map<String, Set<String>> dependencyMap = new HashMap<>();

    public Set<String> getClasses() {
        return dependencyMap.keySet();
    }

    public Set<String> getDependencies(String classname) {
        return dependencyMap.getOrDefault(classname, Collections.emptySet());
    }

    void addDependency(String currentClass, String dependentClass) {
        Set<String> dependencies = dependencyMap.computeIfAbsent(currentClass, k -> new TreeSet<>());
        dependencies.add(dependentClass);
    }
}

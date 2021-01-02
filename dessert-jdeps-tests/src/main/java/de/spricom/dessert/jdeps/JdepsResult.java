package de.spricom.dessert.jdeps;

import java.util.*;

public class JdepsResult {
    private final Map<String, Set<String>> dependencyMap = new HashMap<String, Set<String>>();

    public Set<String> getClasses() {
        return dependencyMap.keySet();
    }

    public Set<String> getDependencies(String classname) {
        return dependencyMap.getOrDefault(classname, Collections.emptySet());
    }

    void addDependency(String currentClass, String dependentClass) {
        Set<String> dependencies = dependencyMap.get(currentClass);
        if (dependencies == null) {
            dependencies = new TreeSet<String>();
            dependencyMap.put(currentClass, dependencies);
        }
        dependencies.add(dependentClass);
    }
}

package de.spricom.dessert.samples;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Root;
import de.spricom.dessert.util.ClassUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class JdkClassesTest {

    @Test
    void showUri() throws IOException {
        System.out.println(ClassUtils.getURI(List.class));
        URL resource = List.class.getResource("List.class");
        System.out.println(resource);
        System.out.println(List.class.getResource("/java/util"));
        System.out.println(List.class.getResource("/java/util/"));
        System.out.println(List.class.getResource("/"));
    }

    @Disabled
    @Test
    void packageContent() throws IOException {
        URL resource = List.class.getResource("List.class");
        System.out.println(resource.getContent());
        System.out.println(resource.getPath());
        URL parent = new URL(resource, "/java.base/java/util");
        System.out.println(parent);
        System.out.println(parent.getContent());
    }

    @Test
    void showDependencies() throws IOException {
        var cf = new ClassFile(List.class);
        cf.getDependentClasses().forEach(System.out::println);
    }

    @Disabled
    @Test
    void root() {
        Classpath cp = new Classpath();
        Root root = cp.rootOf(List.class);
        System.out.println(root.getURI());
    }
}

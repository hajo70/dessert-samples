package de.spricom.dessert.jdeps;

import de.spricom.dessert.slicing.SliceEntry;
import de.spricom.dessert.util.LookupUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.event.TreeSelectionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

public class JdepsWrapperTest {

    private JdepsWrapper wrapper;

    @BeforeEach
    void init() {
        wrapper = new JdepsWrapper();
    }

    @Test
    void testGetJdepsVersion() throws IOException, InterruptedException {
        String version = wrapper.getJdepsVersion();
        System.out.println("jdeps-version: " + version);
        assertThat(version).isNotEmpty();
    }

    @Test
    void testAnalyze() throws IOException, InterruptedException {
        wrapper.addOptions("--multi-release", "base");
        JdepsResult result = wrapper.analyze(getClassesDirectory());
        Set<String> resultDependencies = result.getDependencies(JdepsResult.class.getName());

        assertThat(result.getClasses()).contains(JdepsWrapper.class.getName(), JdepsResult.class.getName());
        assertThat(resultDependencies).contains(TreeSet.class.getName());
    }

    private File getClassesDirectory() {
        return LookupUtil.getRootFile(JdepsWrapper.class);
    }
}

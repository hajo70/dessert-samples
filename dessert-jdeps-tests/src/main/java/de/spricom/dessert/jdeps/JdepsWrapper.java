package de.spricom.dessert.jdeps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uses jdeps to determine all dependencies of all classes within some root (jar file or directory).
 *
 */
public class JdepsWrapper {
    private static final Logger log = LogManager.getLogger(JdepsWrapper.class);

    // The '-' appears in package-info.class, '$' is required for inner classes.
    private static final Pattern SRC_REGEX = Pattern.compile("^\\s+([\\w.$-]+)\\s+.*");
    private static final Pattern DEST_REGEX = Pattern.compile("^.*-> ([\\w.$]+)\\s+.*$");

    private String classPath = System.getProperty("java.class.path");
    private String classPathOption = "-cp";
    private String jdepsCommand = System.getProperty("jdeps.command","jdeps");
    private List<String> options = new ArrayList<>(Arrays.asList("-verbose:class", "-filter:none"));

    public JdepsWrapper() {
    }

    public String getJdepsVersion() throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(jdepsCommand);
        command.add("-version");
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        StringBuilder dump = new StringBuilder();
        if (log.isDebugEnabled()) {
            dump.append(String.join(" ", pb.command())).append("\n");
        }
        Process p = pb.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String version = null;
        String line;
        while ((line = in.readLine()) != null) {
            if (log.isDebugEnabled()) {
                dump.append(line).append("\n");
            }
            version = line;
        }
        int exitCode = p.waitFor();
        log.debug(dump);
        if (exitCode != 0) {
            throw new IllegalStateException(String.join(" ", pb.command()) + " failed!");
        }
        return version;
    }

    public JdepsResult analyze(File path) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(getCommand(path));
        pb.redirectErrorStream(true);
        StringBuilder dump = new StringBuilder();
        if (log.isDebugEnabled()) {
            dump.append(String.join(" ", pb.command())).append("\n");
        }
        JdepsResult result = new JdepsResult();
        Process p = pb.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        String currentClass = "none";
        while ((line = in.readLine()) != null) {
            if (log.isDebugEnabled()) {
                dump.append(line).append("\n");
            }
            Matcher src = SRC_REGEX.matcher(line);
            if (src.matches()) {
                currentClass = src.group(1);
            }
            Matcher dest = DEST_REGEX.matcher(line);
            if (dest.matches()) {
                String dependentClass = dest.group(1);
                result.addDependency(currentClass, dependentClass);
            }
        }
        int exitCode = p.waitFor();
        log.debug(dump);
        if (exitCode != 0) {
            throw new IllegalStateException(String.join(" ", pb.command()) + " failed!");
        }
        return result;
    }

    private List<String> getCommand(File path) {
        List<String> command = new ArrayList<>();
        command.add(jdepsCommand);
        command.addAll(options);
        command.add(classPathOption);
        command.add(classPath);
        command.add(path.getPath());
        return command;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getJdepsCommand() {
        return jdepsCommand;
    }

    public void setJdepsCommand(String jdepsCommand) {
        this.jdepsCommand = jdepsCommand;
    }

    public String getClassPathOption() {
        return classPathOption;
    }

    public void setClassPathOption(String classPathOption) {
        this.classPathOption = classPathOption;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void addOptions(List<String> options) {
        this.options.addAll(options);
    }

    public void addOptions(String... options) {
        addOptions(Arrays.asList(options));
    }
}

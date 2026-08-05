package utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class ClassFinder {

    private static final List<String> EXCLUDED_PREFIXES = List.of(
            "org.",
            "com.fasterxml.",
            "javax.",
            "jakarta.",
            "kotlin.",
            "scala.",
            "META-INF."
    );

    private ClassFinder() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static List<Class<?>> getAllClasses() throws ClassNotFoundException, IOException, URISyntaxException {
        List<Class<?>> classes = new ArrayList<>();
        for (String className : findClassNames()) {
            if (isApplicationClass(className)) {
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }

    private static List<String> findClassNames() throws IOException, URISyntaxException {
        URL location = ClassFinder.class.getProtectionDomain().getCodeSource().getLocation();
        Path path = Paths.get(location.toURI());

        if (Files.isDirectory(path)) {
            return findClassesInDirectory(path);
        }
        return findClassesInJar(path);
    }

    private static List<String> findClassesInDirectory(Path root) throws IOException {
        List<String> classNames = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(file -> file.toString().endsWith(".class"))
                    .map(file -> toClassName(root.relativize(file).toString()))
                    .forEach(classNames::add);
        }
        return classNames;
    }

    private static List<String> findClassesInJar(Path jarPath) throws IOException {
        List<String> classNames = new ArrayList<>();
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class") && !entry.isDirectory() && !name.startsWith("META-INF/")) {
                    classNames.add(toClassName(name));
                }
            }
        }
        return classNames;
    }

    private static String toClassName(String path) {
        return path.replace('\\', '.')
                .replace('/', '.')
                .replaceAll("\\.class$", "");
    }

    private static boolean isApplicationClass(String className) {
        if (className.contains("$") || className.endsWith("module-info")) {
            return false;
        }
        for (String prefix : EXCLUDED_PREFIXES) {
            if (className.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }
}

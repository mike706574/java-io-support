package fun.mike.io.alpha;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IO {
    private static final Logger log = LoggerFactory.getLogger(IO.class);

    public static void mkdir(String path) {
        log.trace(String.format("Creating directory \"%s\".", path));
        if (!new File(path).mkdir()) {
            String message = String.format("Failed to create directory \"%s\".", path);
            throw failure(message);
        }
    }

    public static void copy(String srcPath, String destPath) {
        log.trace(String.format("Copying \"%s\" to \"%s\".", srcPath, destPath));
        try (FileInputStream is = new FileInputStream(srcPath);
             FileChannel ic = is.getChannel();
             FileOutputStream os = new FileOutputStream(destPath);
             FileChannel oc = os.getChannel()) {
            oc.transferFrom(ic, 0, ic.size());
        } catch (IOException ex) {
            String message = String.format("Failed to copy \"%s\" to \"%s\".", srcPath, destPath);
            throw failure(message, ex);
        }
    }

    public static void spit(String path, String content) {
        spit(path, content, false);
    }

    public static void spit(File file, String content) {
        spit(file, content, false);
    }

    public static void spit(String path, String content, boolean append) {
        spit(new File(path), content, append);
    }

    public static void spit(File file, String content, boolean append) {
        try (Writer out = new BufferedWriter(new FileWriter(file, append))) {
            out.write(content);
        } catch (IOException ex) {
            String message = String.format("Failed to spit to \"%s\".",
                                           file.getAbsolutePath());
            throw failure(message, ex);
        }
    }

    public static void withSpitter(String path, Consumer<Spitter> consumer) {
        try (Spitter spitter = new Spitter(path)) {
            consumer.accept(spitter);
        }
    }

    public static String slurp(String path) {
        log.trace(String.format("Slurping from \"%s\".", path));
        try (InputStream is = new URL(path).openConnection().getInputStream()) {
            return slurp(is);
        } catch (MalformedURLException mue) {
            try {
                return new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
            } catch (IOException ex) {
                throw failure(ex);
            }
        } catch (IOException ex) {
            String message = String.format("Failed to slurp from \"%s\".", path);
            throw failure(message, ex);
        }
    }

    public static String slurp(File file) {
        String path = file.getAbsolutePath();
        log.trace(String.format("Slurping from \"%s\".", path));
        try (InputStream is = new FileInputStream(file)) {
            return slurp(is);
        } catch (IOException ex) {
            String message = String.format("Failed to slurp from \"%s\".", path);
            throw failure(message, ex);
        }
    }

    public static String slurp(URI uri) {
        try {
            return slurp(uri.toURL());
        } catch (MalformedURLException ex) {
            String message = String.format("Failed to slurp from \"%s\".", uri.toString());
            throw failure(message, ex);
        }
    }

    public static String slurp(URL url) {
        String path = url.toString();
        log.trace(String.format("Slurping from \"%s\".", path));
        try (InputStream is = url.openConnection().getInputStream()) {
            return slurp(is);
        } catch (IOException ex) {
            String message = String.format("Failed to slurp from \"%s\".", path);
            throw failure(message, ex);
        }
    }

    public static String slurp(InputStream is) {
        try (Reader isReader = new InputStreamReader(is, "UTF-8");
             Reader reader = new BufferedReader(isReader)) {
            StringBuilder stringBuilder = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                stringBuilder.append((char) c);
            }
            return stringBuilder.toString();
        } catch (IOException ex) {
            throw failure("Failed to slurp from input stream.", ex);
        }
    }

    public static String slurpResource(String path) {
        return slurp(Thread.currentThread().getContextClassLoader().getResourceAsStream(path));
    }

    public static void clearDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists() && !directory.isDirectory()) {
            String message = String.format("\"%s\" is not a directory.", path);
            throw new IllegalArgumentException(message);
        }
        log.trace(String.format("Clearing directory \"%s\".", path));
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                log.trace(String.format("Deleting file \"%s\".", file.getAbsolutePath()));
                file.delete();
            }
        }
    }

    public static void nuke(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            for (String filePath : file.list()) {
                nuke(file.getPath() + "/" + filePath);
            }
        }
        file.delete();
    }

    public static void delete(String path) {
        delete(new File(path));
    }

    public static void delete(File file) {
        boolean deleted = file.delete();
        if(!deleted) {
            String message = String.format("Failed to delete \"%s\".", file.getAbsolutePath());
            throw failure(message);
        }
    }

    /**
     * @deprecated Use {@link #nuke(String)} or {@link #delete(String)}.
     */
    @Deprecated
    public static void deleteQuietly(String path) {
        new File(path).delete();
    }

    public static boolean exists(String path) {
        return new File(path).exists();
    }

    public static boolean doesNotExist(String path) {
        return !new File(path).exists();
    }

    public static Stream<String> streamLines(String path) {
        try {
            return Files.lines(Paths.get(path));
        }
        catch (IOException ex) {
            throw failure(ex);
        }
    }

    public static Stream<String> streamLines(String path, int skip) {
        return streamLines(path).skip(skip);
    }

    public static Stream<String> streamLines(String path, int skip, int skipLast) {
        try(Stream<String> stream = streamLines(path)) {
            long count = stream.count();
            return Files.lines(Paths.get(path))
                .limit(Math.max(0, count - skipLast))
                .skip(skip);
        }
        catch (IOException ex) {
            throw failure(ex);
        }
    }

    public static List<String> getLines(String path) {
        return streamLines(path).collect(Collectors.toList());
    }

    public static List<String> getLines(String path, int skip) {
        return streamLines(path, skip).collect(Collectors.toList());
    }

    public static List<String> getLines(String path, int skip, int skipLast) {
        return streamLines(path, skip, skipLast).collect(Collectors.toList());
    }

    public static String slashify(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    public static String uncSlashify(String path) {
        return path.endsWith("\\") ? path : path + "\\";
    }

    public static String pathToUnc(String path) {
        String noStartingSlash = path.startsWith("/") ? path.substring(1) : path;
        return "\\\\" + noStartingSlash.replace("/", "\\");
    }

    public static void chmod(String path, Set<PosixFilePermission> perms) {
        chmod(Paths.get(path), perms);
    }

    public static void chmod(Path path, Set<PosixFilePermission> perms) {
        try {
            Files.setPosixFilePermissions(path, perms);
        } catch (IOException ex) {
            throw failure(ex);
        }
    }

    public static void grantFullAccess(String path) {
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            posixRecursiveChmod(path, fullAccess());
        } else {
            nonPosixRecursiveAccess(path, false);
        }
    }

    public static void posixRecursiveChmod(String path, Set<PosixFilePermission> perms) {
        try {
            Files.walk(Paths.get(path))
                    .forEach(childPath -> chmod(childPath, perms));
        } catch (IOException ex) {
            throw failure(ex);
        }
    }

    public static void nonPosixRecursiveAccess(String path, boolean ownerOnly) {
        try {
            Files.walk(Paths.get(path))
                    .forEach(childPath -> {
                        childPath.toFile().setExecutable(true, ownerOnly);
                        childPath.toFile().setReadable(true, ownerOnly);
                        childPath.toFile().setWritable(true, ownerOnly);
                    });
        } catch (IOException ex) {
            throw failure(ex);
        }
    }

    public static Set<PosixFilePermission> fullAccess() {
        Set<PosixFilePermission> perms = new HashSet<>();

        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);

        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);

        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);

        return perms;
    }

    private static UncheckedIOException failure(String message) {
        return new UncheckedIOException(new IOException(message));
    }

    private static UncheckedIOException failure(String message, IOException ex) {
        return new UncheckedIOException(message, ex);
    }

    private static UncheckedIOException failure(IOException ex) {
        return new UncheckedIOException(ex);
    }
}

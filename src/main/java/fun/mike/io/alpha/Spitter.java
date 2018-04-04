package fun.mike.io.alpha;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;

public class Spitter implements AutoCloseable {
    private final String path;

    private final BufferedWriter writer;

    public Spitter(String path) {
        this.path = path;
        try {
            this.writer = new BufferedWriter(new FileWriter(path));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void spit(String line) {
        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}

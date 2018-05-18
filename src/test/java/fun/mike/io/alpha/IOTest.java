package fun.mike.io.alpha;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IOTest {
    private static final String base = "src/test/resources/";

    private static final String empty = base + "empty.txt";
    private static final String five = base + "five.txt";

    @Test
    public void slurping() throws IOException, URISyntaxException {
        String expected = "foo\n";
        String path = base + "foo.txt";
        File file = new File(path);
        URL url = new URL("file://" + file.getAbsolutePath());

        try(InputStream inputStream = new FileInputStream(file)) {
            assertEquals(expected, IO.slurp(path));
            assertEquals(expected, IO.slurp(file));
            assertEquals(expected, IO.slurp(url));
            assertEquals(expected, IO.slurp(url.toURI()));
            assertEquals(expected, IO.slurp(inputStream));
        }
    }

    @Test
    public void getLines() {
        assertEquals(Collections.emptyList(), IO.getLines(empty));

        assertEquals(Arrays.asList("one", "two", "three", "four", "five"),
                     IO.getLines(five));
        assertEquals(Arrays.asList("two", "three", "four", "five"),
                     IO.getLines(five, 1));
        assertEquals(Arrays.asList("three", "four", "five"),
                     IO.getLines(five, 2));
        assertEquals(Arrays.asList("four", "five"),
                     IO.getLines(five, 3));
        assertEquals(Collections.singletonList("five"),
                     IO.getLines(five, 4));
        assertEquals(Collections.emptyList(),
                     IO.getLines(five, 5));
        assertEquals(Collections.emptyList(),
                     IO.getLines(five, 6));

        assertEquals(Arrays.asList("one", "two", "three", "four"),
                     IO.getLines(five, 0, 1));
        assertEquals(Arrays.asList("one", "two", "three"),
                     IO.getLines(five, 0, 2));
        assertEquals(Arrays.asList("one", "two"),
                     IO.getLines(five, 0, 3));
        assertEquals(Collections.singletonList("one"),
                     IO.getLines(five, 0, 4));
        assertEquals(Collections.emptyList(),
                     IO.getLines(five, 0, 5));
        assertEquals(Collections.emptyList(),
                     IO.getLines(five, 0, 6));
    }

    @Test
    public void streamLines() {
        assertEquals(Collections.emptyList(),
                     IO.streamLines(empty).collect(Collectors.toList()));

        assertEquals(Arrays.asList("one", "two", "three", "four", "five"),
                     IO.streamLines(five).collect(Collectors.toList()));
        assertEquals(Arrays.asList("two", "three", "four", "five"),
                     IO.streamLines(five, 1).collect(Collectors.toList()));
        assertEquals(Arrays.asList("three", "four", "five"),
                     IO.streamLines(five, 2).collect(Collectors.toList()));
        assertEquals(Arrays.asList("four", "five"),
                     IO.streamLines(five, 3).collect(Collectors.toList()));
        assertEquals(Collections.singletonList("five"),
                     IO.streamLines(five, 4).collect(Collectors.toList()));
        assertEquals(Collections.emptyList(),
                     IO.streamLines(five, 5).collect(Collectors.toList()));
        assertEquals(Collections.emptyList(),
                     IO.streamLines(five, 6).collect(Collectors.toList()));

        assertEquals(Arrays.asList("one", "two", "three", "four"),
                     IO.streamLines(five, 0, 1).collect(Collectors.toList()));
        assertEquals(Arrays.asList("one", "two", "three"),
                     IO.streamLines(five, 0, 2).collect(Collectors.toList()));
        assertEquals(Arrays.asList("one", "two"),
                     IO.streamLines(five, 0, 3).collect(Collectors.toList()));
        assertEquals(Collections.singletonList("one"),
                     IO.streamLines(five, 0, 4).collect(Collectors.toList()));
        assertEquals(Collections.emptyList(),
                     IO.streamLines(five, 0, 5).collect(Collectors.toList()));
        assertEquals(Collections.emptyList(),
                     IO.streamLines(five, 0, 6).collect(Collectors.toList()));
    }
}

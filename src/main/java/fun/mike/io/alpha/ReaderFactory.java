package fun.mike.io.alpha;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReaderFactory {
    private static final Logger log = LoggerFactory.getLogger(IO.class);

    public static BufferedReader buffered(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8").newDecoder()));
    }
}

package de.spricom.dessert.test.slicing;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class AccessClassPathTest {

    @Test
    public void test() throws IOException {
        URL url = String.class.getResource("String.class");
        System.out.println("String.class: " + url);
        // System.out.println(new String(readAll(String.class.getResourceAsStream("/java/lang"))));
    }

    private byte[] readAll(InputStream is) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            int c;
            while ((c = is.read()) != -1) {
                os.write(c);
            }
            return os.toByteArray();
        }
    }
}

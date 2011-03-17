package de.logotakt.logolyze.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * An utility class for IO stuff.
 */
public final class IOUtils {

    private IOUtils() {
    }

    /**
     * Read all data from the InputStream and return it as a string.
     * @param is The InputStream to read from.
     * @return All data that was read from the InputStream
     * @throws IOException if the input stream throws it.
     */
    public static String slurpInputStream(final InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }

        return sb.toString();
    }
}

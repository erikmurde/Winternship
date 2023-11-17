package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class FileUtil {

    private static final String WRITE_PATH = "src/main/java/main/";

    public static String readResourceFile(String fileName) {
        try (InputStream is = Thread
                .currentThread()
                .getContextClassLoader()
                .getResourceAsStream(fileName)) {

            return readInputStream(is);

        } catch (IOException e) {
            throw new RuntimeException("Could not read file %s!".formatted(fileName));
        }
    }

    public static void writeToFile(String text, String fileName) {
        File file = new File(WRITE_PATH + fileName);

        try (var writer = new FileWriter(file)) {

            writer.write(text);

        } catch (IOException e) {
            throw new RuntimeException("Could not write to file %s in path %s!".formatted(fileName, WRITE_PATH));
        }
    }

    private static String readInputStream(InputStream is) {
        var isReader = new InputStreamReader(is, StandardCharsets.UTF_8);

        return new BufferedReader(isReader)
                .lines()
                .collect(Collectors.joining("\n"));
    }
}

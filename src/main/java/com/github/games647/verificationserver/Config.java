package com.github.games647.verificationserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class Config {

    private static final String FILE_NAME = "config.properties";

    private final Properties config;

    public Config() throws IOException {
        try (InputStream resourceStream = getClass().getResourceAsStream('/' + FILE_NAME)) {
            config = new Properties();
            config.load(resourceStream);
        }
    }

    public void loadFile() throws IOException {
        Path file = Paths.get(FILE_NAME);
        if (Files.notExists(file)) {
            saveConfig();
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            config.load(reader);
        }
    }

    public String get(String key) {
        return config.getProperty(key);
    }

    public Properties getProperties() {
        return config;
    }

    private void saveConfig() throws IOException {
        Path file = Paths.get(FILE_NAME);

        //go to the root folder of the jar
        try (InputStream resourceStream = getClass().getResourceAsStream('/' + FILE_NAME);
             OutputStream out = Files.newOutputStream(file, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            byte[] buf = new byte[1_024];
            int len;
            while ((len = resourceStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }
}

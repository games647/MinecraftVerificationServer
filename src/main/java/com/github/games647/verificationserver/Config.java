package com.github.games647.verificationserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class Config {

    private static final String FILE_NAME = "config.properties";

    private Properties config;

    public Config() throws IOException {
        try (InputStream resourceStream = getClass().getResourceAsStream('/' + FILE_NAME)) {
            config = new Properties();
            config.load(resourceStream);
        }
    }

    public void loadFile() throws IOException {
        File file = new File(FILE_NAME);
        Path path = file.toPath();

        if (!file.exists()) {
            saveConfig();
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            config.load(reader);
        }
    }

    public String get(String key) {
        return config.getProperty(key);
    }

    public Properties getProperties() {
        return config;
    }

    public void verify() {

    }

    private void saveConfig() throws IOException {
        File file = new File(FILE_NAME);
        Path path = file.toPath();

        //go to the root folder of the jar
        InputStream resourceStream = getClass().getResourceAsStream('/' + FILE_NAME);
        try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            byte[] buf = new byte[1_024];
            int len;
            while ((len = resourceStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (resourceStream != null) {
                resourceStream.close();
            }
        }
    }
}

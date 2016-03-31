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
        InputStream resourceStream = null;
        try {
            resourceStream = getClass().getResourceAsStream('/' + FILE_NAME);
            config = new Properties();
            config.load(resourceStream);
        } finally {
            if (resourceStream != null) {
                resourceStream.close();
            }
        }
    }

    public void loadFile() throws IOException {
        File file = new File(FILE_NAME);
        Path path = file.toPath();

        if (!file.exists()) {
            saveConfig();
        }

        BufferedReader reader = null;
        try {
            reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
            config.load(reader);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public String get(String key) {
        return config.getProperty(key);
    }

    public void verify() {

    }

    private void saveConfig() throws IOException {
        File file = new File(FILE_NAME);
        Path path = file.toPath();

        InputStream resourceStream = getClass().getResourceAsStream('/' + FILE_NAME);
        OutputStream out = null;
        try {
            out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

            byte[] buf = new byte[1024];
            int len;
            while ((len = resourceStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (out != null) {
                out.close();
            }

            if (resourceStream != null) {
                resourceStream.close();
            }
        }
    }
}

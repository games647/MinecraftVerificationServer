package com.github.games647.verificationserver;

import java.security.SecureRandom;
import java.util.stream.IntStream;

public class TokenGenerator {

    private static final char[] TOKEN_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            .toCharArray();

    private final int size;
    protected final SecureRandom random = new SecureRandom();

    public TokenGenerator(int size) {
        this.size = size;
    }

    public String generateToken() {
        StringBuilder tokenBuilder = new StringBuilder();
        IntStream.rangeClosed(1, size)
                .map(i -> random.nextInt(TOKEN_CHARACTERS.length - 1))
                .mapToObj(pos -> TOKEN_CHARACTERS[pos])
                .forEach(tokenBuilder::append);

        return tokenBuilder.toString();
    }
}

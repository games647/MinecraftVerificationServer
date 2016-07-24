package com.github.games647.verificationserver;

import java.security.SecureRandom;

public class TokenGenerator {

    private static final char[] TOKEN_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            .toCharArray();

    private final int size;
    protected SecureRandom random = new SecureRandom();

    public TokenGenerator(int size) {
        this.size = size;
    }

    public String generateToken() {
        StringBuilder tokenBuilder = new StringBuilder();
        for (int i = 1; i <= size; i++) {
            tokenBuilder.append(TOKEN_CHARACTERS[random.nextInt(TOKEN_CHARACTERS.length - 1)]);
        }

        return tokenBuilder.toString();
    }
}

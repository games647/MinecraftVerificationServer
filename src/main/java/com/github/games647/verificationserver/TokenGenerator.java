package com.github.games647.verificationserver;

import java.security.SecureRandom;

public class TokenGenerator {

    private final int size;
    protected SecureRandom random = new SecureRandom();

    public TokenGenerator(int size) {
        this.size = size;
    }

    public String generateToken() {
        StringBuilder tokenBuilder = new StringBuilder();
        for (int i = 1; i <= size; i++) {
            tokenBuilder.append(random.nextInt());
        }

        return tokenBuilder.toString();
    }
}

package com.corefit.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretKeyGenerator {

    public static void main(String[] args) {

        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[32];
        secureRandom.nextBytes(key);

        String secretKey = Base64.getEncoder().encodeToString(key);

        System.out.println("Generated Secret Key: " + secretKey);
    }
}

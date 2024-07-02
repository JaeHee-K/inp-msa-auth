package com.inp.msa.inpmsaauth.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class SecurityUtil {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int SECRET_LENGTH = 32;

    public static String generateSecret() {
        byte[] bytes = new byte[SECRET_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}

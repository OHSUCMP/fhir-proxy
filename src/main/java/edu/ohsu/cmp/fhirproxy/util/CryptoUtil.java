package edu.ohsu.cmp.fhirproxy.util;

import java.security.SecureRandom;

public class CryptoUtil {
    public static byte[] randomBytes(int length) {
        byte[] b = new byte[length];
        new SecureRandom().nextBytes(b);
        return b;
    }
}

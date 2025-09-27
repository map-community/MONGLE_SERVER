package com.algangi.mongle.global.utils;

import java.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public final class PemUtils {

    private PemUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static PrivateKey loadPrivateKey(String keyPath)
        throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        if (keyPath == null || keyPath.isBlank()) {
            throw new IllegalArgumentException("Private Key 경로가 비어 있습니다.");
        }
        String sanitizedPath = keyPath.replace("classpath:", "");
        if (!sanitizedPath.startsWith("/")) {
            sanitizedPath = "/" + sanitizedPath;
        }

        try (InputStream is = PemUtils.class.getResourceAsStream(sanitizedPath)) {
            if (is == null) {
                throw new IOException("Private Key 파일을 찾을 수 없습니다: " + sanitizedPath);
            }

            String privateKeyPEM = new String(is.readAllBytes());

            privateKeyPEM = privateKeyPEM
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("\\R", "")
                .replace("-----END PRIVATE KEY-----", "");

            byte[] decodedKey = Base64.getDecoder().decode(privateKeyPEM);

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        }
    }
}

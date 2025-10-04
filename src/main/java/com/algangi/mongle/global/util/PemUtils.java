package com.algangi.mongle.global.util;

import java.util.Base64;

import java.io.FileInputStream;
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

    public static PrivateKey loadPrivateKey(String filePath)
        throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Private Key 파일 경로가 비어 있습니다.");
        }

        try (InputStream is = new FileInputStream(filePath)) {
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

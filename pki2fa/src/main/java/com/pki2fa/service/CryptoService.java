package com.pki2fa.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
public class CryptoService {

    public String decryptSeed(String encryptedSeedB64, String privateKeyPem) throws Exception {


        encryptedSeedB64 = encryptedSeedB64
                .replaceAll("\\s+", "")
                .trim();


        String cleanedKey = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "")
                .trim();


        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);

        PrivateKey privateKey;
        try {

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception e) {

            throw new RuntimeException("Key must be in PKCS#8 format. Convert using: openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in student_private.pem -out student_private_pkcs8.pem");
        }


        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedSeedB64);


        OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT
        );

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);

        byte[] decrypted = cipher.doFinal(encryptedBytes);

        String hexSeed = new String(decrypted, StandardCharsets.UTF_8).trim();


        if (hexSeed.length() != 64)
            throw new RuntimeException("Invalid seed length");


        Files.writeString(Path.of("/data/seed.txt"), hexSeed);

        return hexSeed;

    }
}


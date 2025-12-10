package com.pki2fa.controller;

import com.pki2fa.service.CryptoService;
import com.pki2fa.service.TotpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
public class TotpController {

    private final CryptoService cryptoService;
    private final TotpService totpService;

    private String cachedSeed = null;

    public TotpController(CryptoService cryptoService, TotpService totpService) {
        this.cryptoService = cryptoService;
        this.totpService = totpService;
    }

    @GetMapping("/totp")
    public String getTotp() throws Exception {
        String seed = loadSeed();
        return totpService.generateTotp(seed);
    }


    @PostMapping("/decrypt-seed")
    public ResponseEntity<?> decryptSeed(@RequestBody Map<String, String> request) {
        try {
            String encryptedSeed = request.get("encrypted_seed").trim();
            System.out.println(encryptedSeed);
            String privateKey = Files.readString(Path.of("/data/student_private.pem")).trim();


            cryptoService.decryptSeed(encryptedSeed, privateKey);
            return ResponseEntity.ok(Map.of("status", "ok"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Decryption failed"+e));
        }
    }

    @GetMapping("/generate-2fa")
    public ResponseEntity<?> generate2fa() {
        try {
            String hexSeed = Files.readString(Path.of("/data/seed.txt")).trim();
            String code = totpService.generateTotp(hexSeed);

            return ResponseEntity.ok(Map.of(
                    "code", code,
                    "valid_for", 30
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Seed not decrypted yet"));
        }
    }



    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request) {

        if (request == null || !request.containsKey("code") || request.get("code") == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing code"));
        }

        String code = request.get("code").trim();
        if (code.length() != 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing code"));
        }

        try {

            Path seedPath = Path.of("/data/seed.txt");
            if (!Files.exists(seedPath)) {
                return ResponseEntity.status(500)
                        .body(Map.of("error", "Seed not decrypted yet"));
            }


            String hexSeed = Files.readString(seedPath).trim();


            boolean valid = totpService.verifyTotp(hexSeed, code, 1);

            return ResponseEntity.ok(Map.of("valid", valid));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Seed not decrypted yet"));
        }
    }


    private String loadSeed() throws Exception {
        if (cachedSeed != null) return cachedSeed;

        Path encPath = Path.of("/data/encrypted_seed.txt");
        Path keyPath = Path.of("/data/student_private.pem");

        String encryptedSeed = Files.readString(encPath).trim();
        String privateKey = Files.readString(keyPath).trim();

        cachedSeed = cryptoService.decryptSeed(encryptedSeed, privateKey);
        return cachedSeed;
    }
}


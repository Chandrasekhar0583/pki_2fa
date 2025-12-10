

package com.pki2fa.service;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class TotpService {

    private static final int TOTP_DIGITS = 6;
    private static final int TIME_STEP = 30;

    public String generateTotp(String hexSeed) throws Exception {
        byte[] seedBytes = hexToBytes(hexSeed);

        Base32 base32 = new Base32();
        String base32Seed = base32.encodeToString(seedBytes);

        return generateCode(base32Seed, getCurrentTimeStep());
    }

    public boolean verifyTotp(String hexSeed, String code, int window) throws Exception {
        byte[] seedBytes = hexToBytes(hexSeed);
        Base32 base32 = new Base32();
        String base32Seed = base32.encodeToString(seedBytes);

        long currentStep = getCurrentTimeStep();

        for (int i = -window; i <= window; i++) {
            String expected = generateCode(base32Seed, currentStep + i);
            if (expected.equals(code))
                return true;
        }
        return false;
    }

    private long getCurrentTimeStep() {
        return System.currentTimeMillis() / 1000 / TIME_STEP;
    }

    private String generateCode(String base32Seed, long timestep) throws Exception {
        Base32 base32 = new Base32();
        byte[] keyBytes = base32.decode(base32Seed);

        byte[] timeBytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            timeBytes[i] = (byte) (timestep & 0xFF);
            timestep >>= 8;
        }

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(keyBytes, "HmacSHA1"));
        byte[] hash = mac.doFinal(timeBytes);

        int offset = hash[hash.length - 1] & 0x0F;

        int binary = ((hash[offset] & 0x7F) << 24) |
                ((hash[offset + 1] & 0xFF) << 16) |
                ((hash[offset + 2] & 0xFF) << 8) |
                (hash[offset + 3] & 0xFF);

        return String.format("%06d", binary % 1_000_000);
    }

    private byte[] hexToBytes(String hex) {
        byte[] arr = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            arr[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return arr;
    }
}


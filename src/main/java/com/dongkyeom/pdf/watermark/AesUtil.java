package com.dongkyeom.pdf.watermark;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 암호화 & 복호화 유틸 (간단 예시)
 */
public class AesUtil {

    public static String encryptAES(String plain, byte[] key) throws Exception {

        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(iv));

        byte[] encrypted = cipher.doFinal(plain.getBytes("UTF-8"));

        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decryptAES(String cipherBase64, byte[] key) throws Exception {
        byte[] combined = Base64.getDecoder().decode(cipherBase64);

        byte[] iv = new byte[16];
        byte[] cipherBytes = new byte[combined.length - 16];
        System.arraycopy(combined, 0, iv, 0, 16);
        System.arraycopy(combined, 16, cipherBytes, 0, cipherBytes.length);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv));

        byte[] plain = cipher.doFinal(cipherBytes);
        return new String(plain, "UTF-8");
    }
}

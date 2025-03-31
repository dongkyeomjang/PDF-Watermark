package com.dongkyeom.pdf.watermark;

import net.minidev.json.JSONObject;

public class WatermarkGenerator {

    /**
     * 예: JSON 형태로 유저 정보를 만들고, AES 암호화 + Base64
     */
    public static String generateEncryptedWatermark(String userName, String phone, String date, byte[] aesKey) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("UserName", userName);
            obj.put("Phone", phone);
            obj.put("Date", date);
            obj.put("Rand", System.nanoTime());

            String raw = obj.toString();

            String enc = AesUtil.encryptAES(raw, aesKey);
            return enc;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * AES + Base64 로 암호화된 워터마크 복호화
     */
    public static String decryptWatermark(String cipherText, byte[] aesKey) {
        try {
            return AesUtil.decryptAES(cipherText, aesKey);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
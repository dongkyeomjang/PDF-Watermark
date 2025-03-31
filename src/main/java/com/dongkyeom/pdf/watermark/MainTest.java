package com.dongkyeom.pdf.watermark;

public class MainTest {
    public static void main(String[] args) {
        String input = "/Users/kyeom/Desktop/Project/PDF-Watermark/src/main/resources/test.pdf";
        String output = "/Users/kyeom/Desktop/Project/PDF-Watermark/src/main/resources/watermarked.pdf";

        // 16바이트 AES key
        byte[] aesKey = "MyKey1234MyKey12".getBytes();

        // 예시 사용자
        String userName = "장동겸";
        String phone = "010-1234-5678";
        String date = "2025-03-31";

        // 삽입
        PdfWatermarkEmbedder.embedWatermark(input, output, userName, phone, date, aesKey);

        // 추출 (하나라도 찾으면 OK)
        String found = PdfWatermarkExtractor.extractOneWatermark(output, aesKey);
        if(found != null) {
            System.out.println("[FOUND WATERMARK] " + found);
        } else {
            System.out.println("워터마크 전혀 발견되지 않음");
        }
    }
}
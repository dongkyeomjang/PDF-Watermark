package com.dongkyeom.pdf.watermark;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class PdfWatermarkEmbedder {

    /**
     * AES key는 16바이트 배열 ex) "MyKey1234MyKey12".getBytes()
     */
    public static void embedWatermark(String inputPdf, String outputPdf,
                                      String userName, String phone, String date, byte[] aesKey) {
        PDDocument doc = null;
        try {
            File f = new File(inputPdf);
            RandomAccessRead rar = new RandomAccessReadBufferedFile(f);

            PDFParser parser = new PDFParser(rar);
            COSDocument cosDoc = parser.parse().getDocument();
            doc = new PDDocument(cosDoc, rar);

            // AES 암호화된 워터마크
            String encryptedWatermark = WatermarkGenerator.generateEncryptedWatermark(userName, phone, date, aesKey);

            // Annotation 여러 개 삽입
            insertAnnotations(doc, encryptedWatermark);

            // 메타데이터에 여러 난수 key 삽입
            insertMetadataMultiple(doc, encryptedWatermark);

            doc.save(outputPdf);
            System.out.println("[INFO] Embed done => " + outputPdf);

        } catch(InvalidPasswordException e) {
            System.err.println("PDF is encrypted, can't embed watermark easily!");
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(doc != null) {
                try { doc.close(); } catch (IOException ignored){}
            }
        }
    }

    /**
     * 여러 페이지, 여러개의 Annotation 삽입.
     * "Ctrl+F"로 쉽게 찾을 수 있는 접두어 없이,
     * AES+Base64 문자열만 넣음.
     */
    private static void insertAnnotations(PDDocument doc, String encWM) throws IOException {
        int pageCount = doc.getNumberOfPages();
        Random rand = new Random();
        for (int i = 0; i < pageCount; i++) {
            PDPage page = doc.getPage(i);

            // 페이지마다 2~4개의 annotation
            int howMany = 2 + rand.nextInt(3);
            for(int j=0; j<howMany; j++){
                PDAnnotationText ann = new PDAnnotationText();
                ann.setContents(encWM);
                ann.setHidden(true);
                ann.setInvisible(true);

                // 화면 바깥
                float offX = -10000 - j*100;
                float offY = -10000 - (i*100) - (j*50);
                ann.setRectangle(new PDRectangle(offX, offY, 0, 0));

                page.getAnnotations().add(ann);
            }
        }
    }

    /**
     * 메타데이터에 5개 key를 "난수 Key"로 중복 삽입.
     * 예: A1680123456_Ab, B1680123456_Zb, ...
     */
    private static void insertMetadataMultiple(PDDocument doc, String encWM) throws IOException {
        PDMetadata metadata = new PDMetadata(doc);
        COSDictionary cos = metadata.getCOSObject();

        Random rand = new Random();

        for(int i=0; i<5; i++){
            long stamp = System.nanoTime();
            String randomKey = generateRandomKey(stamp, rand);
            cos.setString(COSName.getPDFName(randomKey), encWM);
        }

        doc.getDocumentCatalog().setMetadata(metadata);
    }

    // 난수 key 생성
    private static String generateRandomKey(long stamp, Random rand) {
        StringBuilder sb = new StringBuilder();

        sb.append((char)('A' + rand.nextInt(26)));
        sb.append(stamp);
        sb.append("_");

        sb.append((char)('A'+rand.nextInt(26)));
        sb.append((char)('a'+rand.nextInt(26)));
        return sb.toString();
    }
}

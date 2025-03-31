package com.dongkyeom.pdf.watermark;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;

import java.io.File;
import java.io.IOException;

public class PdfWatermarkExtractor {
    /**
     * 모든 Annotation / 메타데이터 Key를 순회,
     * AES 복호화 -> 첫 번째 성공 결과 즉시 반환
     */
    public static String extractOneWatermark(String pdfPath, byte[] aesKey) {
        PDDocument doc = null;
        try {
            File f = new File(pdfPath);
            PDFParser parser = new PDFParser(new RandomAccessReadBufferedFile(f));
            COSDocument cosDoc = parser.parse().getDocument();
            doc = new PDDocument(cosDoc);

            // Annotation
            for (PDPage page : doc.getPages()) {
                for (PDAnnotation ann : page.getAnnotations()) {
                    if (ann instanceof PDAnnotationText) {
                        String c = ann.getContents();
                        if(c != null && !c.isBlank()) {
                            try {
                                String dec = WatermarkGenerator.decryptWatermark(c, aesKey);
                                // 첫 번째 성공 시 반환
                                return dec;
                            } catch(Exception ignored){}
                        }
                    }
                }
            }

            // Metadata
            PDMetadata metadata = doc.getDocumentCatalog().getMetadata();
            if(metadata != null) {
                COSDictionary mdDict = metadata.getCOSObject();
                for(COSName cn : mdDict.keySet()) {
                    String val = mdDict.getString(cn);
                    if(val != null && !val.isBlank()) {
                        try {
                            String dec = WatermarkGenerator.decryptWatermark(val, aesKey);
                            return dec;
                        } catch(Exception ignored){}
                    }
                }
            }

        } catch(Exception e){
            e.printStackTrace();
        } finally {
            if(doc != null) {
                try { doc.close(); } catch (IOException ignored){}
            }
        }
        return null;
    }
}

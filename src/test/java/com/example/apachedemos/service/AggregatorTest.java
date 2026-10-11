package com.example.apachedemos.service;

import org.apache.tika.exception.TikaConfigException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.ocr.TesseractOCRParser;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AggregatorTest {

    private final PdfExtractionService pdfExtractionService = new PdfExtractionService();

    private final OCRTextExtrcationService ocrTextExtrcationService = new OCRTextExtrcationService();

    public AggregatorTest() throws TikaConfigException { }

    @Test
    void should_extract_image_and_text_from_image() throws IOException, TikaException, SAXException {
        Assumptions.assumeTrue(new TesseractOCRParser().hasTesseract(),
                "Tesseract must be installed and available on PATH to run OCR integration tests.");

        ClassPathResource pdf = new ClassPathResource("testpdfs/example_image.pdf");
        MultipartFile examplePdf = new MockMultipartFile(
                "file", "example_image.pdf", "application/pdf", pdf.getInputStream());
        Map<String, Object> result = pdfExtractionService.extract(examplePdf);

        var images = (List<Map<String, Object>>) result.get("images");
        assertEquals(1, images.size());

        var image = (String) images.get(0).get("data");
        var contentType = (String) images.get(0).get("contentType");

        var extractedText = ocrTextExtrcationService.extractText(contentType, image.getBytes());
        assertFalse(extractedText.isBlank());
    }
}

package com.example.apachedemos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class PdfExtractionServiceTest {

    private final PdfExtractionService pdfExtractionService = new PdfExtractionService();

    @Mock
    private MultipartFile file;

    @Test
    void extractsTextAndMetadataFromPdf() throws Exception {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("sample.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(createPdf("Hello from PDF.")));

        Map<String, Object> result = pdfExtractionService.extract(file);

        assertEquals("sample.pdf", result.get("filename"));
        assertEquals("Hello from PDF.", result.get("content"));
        assertEquals("Hello from PDF.".length(), result.get("contentLength"));
        assertInstanceOf(Map.class, result.get("metadata"));
        verify(file).getInputStream();
    }

    @Test
    void extractsTextFromExamplePdf() throws Exception {
        ClassPathResource pdf = new ClassPathResource("testpdfs/example_text.pdf");
        MultipartFile examplePdf = new MockMultipartFile(
                "file", "example_image.pdf", "application/pdf", pdf.getInputStream());

        Map<String, Object> result = pdfExtractionService.extract(examplePdf);

        assertFalse(((String) result.get("content")).isBlank());
    }

    @Test
    void extractsImageOnlyPdf() throws Exception {
        ClassPathResource pdf = new ClassPathResource("testpdfs/example_image.pdf");
        MultipartFile examplePdf = new MockMultipartFile(
                "file", "example_image.pdf", "application/pdf", pdf.getInputStream());

        Map<String, Object> result = pdfExtractionService.extract(examplePdf);

        assertEquals("example_image.pdf", result.get("filename"));
        assertEquals(0, result.get("contentLength"));
        assertEquals("", result.get("content"));
        assertInstanceOf(Map.class, result.get("metadata"));
    }

    @Test
    void rejectsNullFile() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pdfExtractionService.extract(null));

        assertEquals("Uploaded file is missing or empty.", exception.getMessage());
    }

    @Test
    void rejectsEmptyFile() {
        when(file.isEmpty()).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pdfExtractionService.extract(file));

        assertEquals("Uploaded file is missing or empty.", exception.getMessage());
    }

    @Test
    void rejectsNonPdfFile() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("sample.txt");
        when(file.getContentType()).thenReturn("text/plain");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pdfExtractionService.extract(file));

        assertEquals("Uploaded file must be a PDF.", exception.getMessage());
    }

    private byte[] createPdf(String text) throws IOException {
        String stream = "BT /F1 12 Tf 72 720 Td (" + text + ") Tj ET";
        String[] objects = {
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 4 0 R >> >>"
                        + " /MediaBox [0 0 612 792] /Contents 5 0 R >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
                "<< /Length " + stream.length() + " >>\nstream\n" + stream + "\nendstream"
        };

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        pdf.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
        Map<Integer, Integer> offsets = new LinkedHashMap<>();
        for (int index = 0; index < objects.length; index++) {
            offsets.put(index + 1, pdf.size());
            pdf.write((index + 1 + " 0 obj\n" + objects[index] + "\nendobj\n").getBytes(StandardCharsets.US_ASCII));
        }

        int xrefOffset = pdf.size();
        pdf.write(("xref\n0 " + (objects.length + 1) + "\n0000000000 65535 f \n").getBytes(StandardCharsets.US_ASCII));
        for (int index = 1; index <= objects.length; index++) {
            pdf.write(String.format("%010d 00000 n \n", offsets.get(index)).getBytes(StandardCharsets.US_ASCII));
        }
        pdf.write(("trailer\n<< /Size " + (objects.length + 1) + " /Root 1 0 R >>\nstartxref\n"
                + xrefOffset + "\n%%EOF\n").getBytes(StandardCharsets.US_ASCII));
        return pdf.toByteArray();
    }
}

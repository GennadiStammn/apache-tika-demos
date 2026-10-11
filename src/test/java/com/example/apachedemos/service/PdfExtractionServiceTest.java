package com.example.apachedemos.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfExtractionServiceTest {

    private final PdfExtractionService pdfExtractionService = new PdfExtractionService();

    @Mock
    private MultipartFile file;

    @Test
    void extracts_text_from_pdf() throws Exception {
        ClassPathResource pdf = new ClassPathResource("testpdfs/example_text.pdf");
        MultipartFile examplePdf = new MockMultipartFile(
                "file", "example_image.pdf", "application/pdf", pdf.getInputStream());

        Map<String, Object> result = pdfExtractionService.extract(examplePdf);

        assertFalse(((String) result.get("content")).isBlank());
    }

    @Test
    void extracts_text_and_images_from_pdf() throws Exception {
        ClassPathResource pdf = new ClassPathResource("testpdfs/example_image.pdf");
        MultipartFile examplePdf = new MockMultipartFile(
                "file", "example_image.pdf", "application/pdf", pdf.getInputStream());

        Map<String, Object> result = pdfExtractionService.extract(examplePdf);

        assertEquals("example_image.pdf", result.get("filename"));
        assertFalse(((String) result.get("content")).isBlank());
        assertInstanceOf(Map.class, result.get("metadata"));
        assertEquals(1, result.get("imageCount"));
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

}

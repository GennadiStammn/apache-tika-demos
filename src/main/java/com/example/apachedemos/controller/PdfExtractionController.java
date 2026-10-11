package com.example.apachedemos.controller;

import java.util.Map;

import com.example.apachedemos.service.PdfExtractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class PdfExtractionController {

    private final PdfExtractionService pdfExtractionService;

    public PdfExtractionController(PdfExtractionService pdfExtractionService) {
        this.pdfExtractionService = pdfExtractionService;
    }

    @PostMapping("/api/extract/pdf")
    public ResponseEntity<Map<String, Object>> extractPdf(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(pdfExtractionService.extract(file));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to parse PDF: " + e.getMessage()));
        }
    }
}

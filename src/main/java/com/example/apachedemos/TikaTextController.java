package com.example.apachedemos;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TikaTextController {

    private final TikaTextService tikaTextService;

    public TikaTextController(TikaTextService tikaTextService) {
        this.tikaTextService = tikaTextService;
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "apache-tika-demos");
    }

    @PostMapping("/api/extract")
    public ResponseEntity<Map<String, Object>> extractText(@RequestParam("file") MultipartFile file) {
        try {
            String content = tikaTextService.extractText(file);
            return ResponseEntity.ok(Map.of(
                    "filename", file.getOriginalFilename(),
                    "contentLength", content.length(),
                    "content", content
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}

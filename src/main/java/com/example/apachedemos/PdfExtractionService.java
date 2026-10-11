package com.example.apachedemos;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

@Service
public class PdfExtractionService {

    public Map<String, Object> extract(MultipartFile file) throws IOException, TikaException, SAXException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is missing or empty.");
        }

        String name = file.getOriginalFilename();
        String type = file.getContentType();
        boolean looksLikePdf = (name != null && name.toLowerCase().endsWith(".pdf"))
                || "application/pdf".equalsIgnoreCase(type);
        if (!looksLikePdf) {
            throw new IllegalArgumentException("Uploaded file must be a PDF.");
        }

        Metadata metadata = new Metadata();
        BodyContentHandler handler = new BodyContentHandler(-1);
        try (InputStream in = file.getInputStream()) {
            new PDFParser().parse(in, handler, metadata, new ParseContext());
        }

        Map<String, String> meta = new LinkedHashMap<>();
        for (String key : metadata.names()) {
            meta.put(key, metadata.get(key));
        }

        String text = handler.toString().trim();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("filename", name);
        result.put("pageCount", meta.get("xmpTPg:NPages"));
        result.put("metadata", meta);
        result.put("contentLength", text.length());
        result.put("content", text);
        return result;
    }
}

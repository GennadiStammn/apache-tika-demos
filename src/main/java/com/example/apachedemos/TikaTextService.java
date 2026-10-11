package com.example.apachedemos;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TikaTextService {

    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is missing or empty.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Parser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();

            parser.parse(inputStream, handler, metadata, context);

            String extracted = handler.toString().trim();
            if (extracted.isBlank()) {
                return tika.parseToString(inputStream);
            }

            return extracted;
        }
    }
}

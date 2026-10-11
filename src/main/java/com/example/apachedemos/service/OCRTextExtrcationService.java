package com.example.apachedemos.service;

import java.io.IOException;
import java.util.Base64;

import org.apache.tika.exception.TikaConfigException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.ocr.TesseractOCRParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

@Service
public class OCRTextExtrcationService {

    private final TesseractOCRParser ocrParser;

    public OCRTextExtrcationService() throws TikaConfigException {
        this(new TesseractOCRParser());
    }

    OCRTextExtrcationService(TesseractOCRParser ocrParser) {
        this.ocrParser = ocrParser;
    }

    public String extractText(String contentType, byte[] base64EncodedImage) throws IOException, TikaException, SAXException {
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Image content type is missing or empty.");
        }
        if (base64EncodedImage == null || base64EncodedImage.length == 0) {
            throw new IllegalArgumentException("Base64-encoded image is missing or empty.");
        }
        if (!ocrParser.hasTesseract()) {
            throw new TikaException("Tesseract OCR is not installed or is not available on PATH.");
        }

        byte[] image;
        try {
            image = Base64.getDecoder().decode(base64EncodedImage);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Image must be Base64-encoded.", e);
        }
        if (image.length == 0) {
            throw new IllegalArgumentException("Base64-encoded image is missing or empty.");
        }

        BodyContentHandler handler = new BodyContentHandler(-1);
        ParseContext context = new ParseContext();
        context.set(TesseractOCRConfig.class, new TesseractOCRConfig());
        Metadata metadata = new Metadata();
        metadata.set(HttpHeaders.CONTENT_TYPE, contentType);
        try (TikaInputStream input = TikaInputStream.get(image)) {
            ocrParser.parse(input, handler, metadata, context);
        }
        return handler.toString().trim();
    }
}

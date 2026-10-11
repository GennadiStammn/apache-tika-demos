package com.example.apachedemos.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRParser;
import org.junit.jupiter.api.Test;
import org.xml.sax.ContentHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OCRTextExtrcationServiceTest {

    @Test
    void passesDecodedImageAndContentTypeToParser() throws Exception {
        TesseractOCRParser parser = mock(TesseractOCRParser.class);
        when(parser.hasTesseract()).thenReturn(true);
        AtomicReference<byte[]> parsedImage = new AtomicReference<>();
        doAnswer(invocation -> {
            TikaInputStream input = invocation.getArgument(0);
            parsedImage.set(input.readAllBytes());
            return null;
        }).when(parser).parse(any(TikaInputStream.class), any(ContentHandler.class), any(Metadata.class),
                any(ParseContext.class));

        OCRTextExtrcationService service = new OCRTextExtrcationService(parser);
        byte[] imageBytes = "image".getBytes(StandardCharsets.UTF_8);
        String image = Base64.getEncoder().encodeToString(imageBytes);

        service.extractText("image/png", image.getBytes(StandardCharsets.UTF_8));

        org.mockito.ArgumentCaptor<Metadata> metadata = org.mockito.ArgumentCaptor.forClass(Metadata.class);
        verify(parser).parse(any(TikaInputStream.class), any(ContentHandler.class), metadata.capture(),
                any(ParseContext.class));
        assertEquals("image", new String(parsedImage.get(), StandardCharsets.UTF_8));
        assertEquals("image/png", metadata.getValue().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void failsClearlyWhenTesseractIsUnavailable() throws Exception {
        TesseractOCRParser parser = mock(TesseractOCRParser.class);
        when(parser.hasTesseract()).thenReturn(false);
        OCRTextExtrcationService service = new OCRTextExtrcationService(parser);

        TikaException exception = assertThrows(TikaException.class,
                () -> service.extractText("image/png", "aW1hZ2U=".getBytes(StandardCharsets.UTF_8)));

        assertEquals("Tesseract OCR is not installed or is not available on PATH.", exception.getMessage());
    }
}

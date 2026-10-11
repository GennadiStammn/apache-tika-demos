package com.example.apachedemos.service;

import org.apache.tika.exception.TikaException;
import org.apache.tika.language.translate.Translator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TranslateServiceTest {

    @Test
    void translatesTextWithTheSpecifiedLanguages() throws Exception {
        Translator translator = mock(Translator.class);
        when(translator.isAvailable()).thenReturn(true);
        when(translator.translate("Hello", "en", "fr")).thenReturn("Bonjour");
        TranslateService service = new TranslateService(translator);

        assertEquals("Bonjour", service.translate("Hello", "en", "fr"));

        verify(translator).translate("Hello", "en", "fr");
    }

    @Test
    void rejectsMissingText() {
        TranslateService service = new TranslateService(mock(Translator.class));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.translate(" ", "en", "fr"));

        assertEquals("Text to translate is missing or empty.", exception.getMessage());
    }

    @Test
    void rejectsMissingSourceLanguage() {
        TranslateService service = new TranslateService(mock(Translator.class));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.translate("Hello", "", "fr"));

        assertEquals("Source language is missing or empty.", exception.getMessage());
    }

    @Test
    void rejectsMissingTargetLanguage() {
        TranslateService service = new TranslateService(mock(Translator.class));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.translate("Hello", "en", " "));

        assertEquals("Target language is missing or empty.", exception.getMessage());
    }

    @Test
    void failsClearlyWhenNoTranslatorIsAvailable() {
        Translator translator = mock(Translator.class);
        when(translator.isAvailable()).thenReturn(false);
        TranslateService service = new TranslateService(translator);

        TikaException exception = assertThrows(TikaException.class,
                () -> service.translate("Hello", "en", "fr"));

        assertEquals("No Apache Tika translators are currently available.", exception.getMessage());
    }

    @Test
    void failsClearlyWhenTranslatorReturnsNoTranslation() throws Exception {
        Translator translator = mock(Translator.class);
        when(translator.isAvailable()).thenReturn(true);
        when(translator.translate("Hello", "en", "fr")).thenReturn(null);
        TranslateService service = new TranslateService(translator);

        TikaException exception = assertThrows(TikaException.class,
                () -> service.translate("Hello", "en", "fr"));

        assertEquals("Apache Tika translator did not return a translation.", exception.getMessage());
    }
}

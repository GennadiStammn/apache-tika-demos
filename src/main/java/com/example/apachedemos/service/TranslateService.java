package com.example.apachedemos.service;

import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.language.translate.DefaultTranslator;
import org.apache.tika.language.translate.Translator;
import org.springframework.stereotype.Service;

@Service
public class TranslateService {

    private final Translator translator;

    public TranslateService() {
        this(new DefaultTranslator());
    }

    TranslateService(Translator translator) {
        this.translator = translator;
    }

    public String translate(String text, String sourceLanguage, String targetLanguage)
            throws TikaException, IOException {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text to translate is missing or empty.");
        }
        if (sourceLanguage == null || sourceLanguage.isBlank()) {
            throw new IllegalArgumentException("Source language is missing or empty.");
        }
        if (targetLanguage == null || targetLanguage.isBlank()) {
            throw new IllegalArgumentException("Target language is missing or empty.");
        }
        if (!translator.isAvailable()) {
            throw new TikaException("No Apache Tika translators are currently available.");
        }

        String translation = translator.translate(text, sourceLanguage, targetLanguage);
        if (translation == null) {
            throw new TikaException("Apache Tika translator did not return a translation.");
        }
        return translation;
    }
}

package com.example.apachedemos.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
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

        byte[] pdf;
        try (InputStream in = file.getInputStream()) {
            pdf = in.readAllBytes();
        }

        Metadata metadata = new Metadata();
        BodyContentHandler handler = new BodyContentHandler(-1);
        List<Map<String, Object>> images = new ArrayList<>();
        ParseContext context = new ParseContext();
        try (TikaInputStream in = TikaInputStream.get(new java.io.ByteArrayInputStream(pdf))) {
            new PDFParser().parse(in, handler, metadata, context);
        }
        extractImages(pdf, images);

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
        result.put("imageCount", images.size());
        result.put("images", images);
        return result;
    }

    private void extractImages(byte[] pdf, List<Map<String, Object>> images) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                collectImages(document.getPage(pageIndex).getResources(), images,
                        Collections.newSetFromMap(new IdentityHashMap<>()));
            }
        }
    }

    private void collectImages(PDResources resources, List<Map<String, Object>> images,
            java.util.Set<PDXObject> visitedObjects) throws IOException {
        if (resources == null) {
            return;
        }

        for (COSName name : resources.getXObjectNames()) {
            PDXObject object = resources.getXObject(name);
            if (object == null || !visitedObjects.add(object)) {
                continue;
            }
            if (object instanceof PDImageXObject image) {
                addImage(image, images);
            } else if (object instanceof PDFormXObject form) {
                collectImages(form.getResources(), images, visitedObjects);
            }
        }
    }

    private void addImage(PDImageXObject image, List<Map<String, Object>> images) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image.getImage(), "png", output)) {
            throw new IOException("Unable to encode an embedded PDF image as PNG.");
        }

        Map<String, Object> extractedImage = new LinkedHashMap<>();
        extractedImage.put("filename", "image-" + (images.size() + 1) + ".png");
        extractedImage.put("contentType", "image/png");
        extractedImage.put("data", Base64.getEncoder().encodeToString(output.toByteArray()));
        images.add(extractedImage);
    }
}

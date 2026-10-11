# apache-tika-demos

A Spring Boot 4.1.1 project with Apache Tika integration.

## Features

- Spring Boot application skeleton
- Apache Tika text extraction from uploaded files
- Simple REST endpoint for content extraction
- OCR text extraction from Base64-encoded image byte arrays with Tesseract

## OCR service

`OCRTextExtrcationService#extractText(String contentType, byte[] base64EncodedImage)` decodes
the supplied Base64 image bytes and extracts its text through Tika's Tesseract OCR parser.
Tesseract must be installed and available on `PATH`; otherwise, the service reports a
`TikaException`. OCR integration tests are skipped when that prerequisite is unavailable.

## Run locally

```bash
mvn spring-boot:run
```

Then open:

- http://localhost:8080/api/health

You can upload a file to:

- POST http://localhost:8080/api/extract
- Form field: `file`

## Example

```bash
curl -F "file=@sample.txt" http://localhost:8080/api/extract
```

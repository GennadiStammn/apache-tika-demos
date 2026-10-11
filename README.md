# apache-tika-demos

A Spring Boot 4.1.1 project with Apache Tika integration.

## Features

- Spring Boot application skeleton
- Apache Tika text extraction from uploaded files
- Simple REST endpoint for content extraction

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

# Smart Document Processing System

## Overview

This application processes business documents (invoices and purchase orders), extracts structured data, validates it, and allows manual review and correction.

The system supports multiple document formats and detects inconsistencies such as incorrect totals, missing fields, and duplicate document numbers.

---

## Tech Stack

- Backend: Java 21, Spring Boot
- Frontend: Angular
- Database: MySQL
- OCR: Tesseract
- AI (optional): Groq API
- Deployment: Docker, NGINX

---

## Features

- Upload documents (CSV, TXT, PDF, Images)
- Automatic data extraction
- Validation engine:
  - Missing fields
  - Invalid totals
  - Date validation
  - Line item validation
  - Duplicate document detection
- Manual review & correction
- Dashboard:
  - Document list
  - Status tracking
  - Validation issues
  - Totals grouped by currency
- File preview & download
- Swagger API documentation

---

## Prerequisites

### For Docker (recommended)

- Docker
- Docker Compose

### For Local Development

- Java 21
- Maven
- Node.js (v18+)
- MySQL
- Tesseract OCR

---

## Run with Docker (Recommended)

### Start application

```bash
docker-compose up -d --build
```

### Access application

**Frontend:**  
http://localhost:8080  

**Backend (Swagger):**  
http://localhost:9005/swagger-ui/index.html  

---

## Run Backend Locally

### 1. Create database

```sql
CREATE DATABASE mastery_task_db;
```

### 2. Configure application.properties

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mastery_task_db
spring.datasource.username=root
spring.datasource.password=your_password

tesseract.datapath=C:/Program Files/Tesseract-OCR/tessdata
```

### 3. Run backend

```bash
cd backend
mvn spring-boot:run
```

---

## Run Frontend Locally

### Install and start

```bash
cd mastery_frontend
npm install
npm start
```

### Access frontend

http://localhost:4200  

---

## Environment Variables

The application uses the following environment variables:

```
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
TESSERACT_DATAPATH
```

---

## API Documentation

Swagger UI:  
http://localhost:9005/swagger-ui/index.html  

---

## Deployment

Live application:  
https://mtask-biljana.online  

---

## Notes

- Tesseract OCR is required for image processing in local setup  
- In Docker environment, Tesseract is already installed  
- The system supports both OCR-based extraction and optional AI-based extraction via Groq API  
- Angular frontend uses environment-based configuration (`/api` in production)  

---

## Explanation of Approach

### Architecture

The application is designed as a full-stack system with a clear separation of concerns between the backend, frontend, and infrastructure layers.

- The **backend (Spring Boot)** is responsible for document processing, data extraction, validation, and persistence.
- The **frontend (Angular)** provides a user interface for uploading documents, reviewing extracted data, and interacting with the system.
- **MySQL** is used for structured storage of documents and extracted metadata.
- **Docker** is used to ensure consistent environment setup and simplify deployment.

Uploaded files are stored on the file system, while the database stores only metadata and file references (such as file path, filename, and type).  
This approach avoids storing large binary data in the database and improves performance and scalability.

The system follows a modular design where parsing, validation, and business logic are separated into dedicated components.

---

### Document Parsing

Different parsing strategies are used depending on the document type:

- **CSV**: Parsed line-by-line with structured mapping into line items.

- **TXT**: Regex-based extraction is used to identify key fields such as document number, supplier, and totals.

- **PDF**:
  - For text-based PDFs, **Apache PDFBox** is used to extract text directly.
  - For scanned PDFs (image-based), **Tesseract OCR (via Tess4J wrapper)** is used.

- **Images**:
  - Primary approach uses **Tesseract OCR (Tess4J)** to extract raw text.
  - Additionally, an optional **AI-based approach (Groq API)** is supported for more flexible extraction of unstructured or low-quality inputs.

After raw text extraction, pattern matching and parsing logic are applied to extract structured fields.

This hybrid approach allows:
- reliable processing of structured documents (via regex and parsing)
- support for scanned or noisy inputs (via OCR)
- improved flexibility for complex documents (via AI)

The design ensures the system is extensible and can easily support additional parsing strategies in the future.

---

### Validation

Validation is implemented as a separate layer after parsing to ensure data consistency.

The validation includes:
- Required field checks (document number, totals, etc.)
- Logical validation (e.g. `subtotal + tax = total`)
- Line item validation (`quantity × unitPrice = lineTotal`)
- Duplicate document detection
- Date validation (issue date vs due date)

Validation issues are collected and stored with the document, allowing the system to distinguish between valid and review-required documents.

---

### Review Workflow

Documents that fail validation are marked with status `NEEDS_REVIEW`.

The user can:
- View extracted data
- See validation issues
- Manually correct fields
- Save the updated document

After manual correction, the document is re-validated to ensure consistency before being marked as `VALIDATED`.

This approach ensures a balance between automation and user control.

---

### File Handling

Uploaded files are:
- Stored on disk (via Docker volume) to ensure persistence across container restarts
- Linked to database records via metadata (filename, type, path)

The system supports:
- File preview (inline viewing)
- File download

This separation ensures efficient storage while keeping database size manageable.

---

### Design Decisions

Several design decisions were made to improve flexibility and maintainability:

- **Separation of parsing and validation**: allows reuse and easier extension of validation rules
- **Environment-based configuration**: enables seamless transition between local and production environments
- **Dockerized setup**: ensures consistent deployment and simplifies onboarding
- **Modular parser design**: each document type has its own parsing strategy
- **Use of OCR + optional AI**: provides robustness for both structured and unstructured documents

The system is designed to be easily extendable with additional document formats, validation rules, and integrations.

---

## AI Tools Used

During development, ChatGPT was used as a support tool for:

- clarifying architectural decisions and best practices  
- generating boilerplate code and improving code structure  
- debugging issues related to Docker, Angular, and Spring Boot  
- refining validation logic and API design  

Additionally, the application includes optional integration with the Groq API for AI-based document extraction from unstructured inputs.

---

## Future Improvements

- Improve OCR accuracy for noisy images  
- Add support for additional business document formats such as DOCX, XML, and JSON
- Add authentication & user roles  
- Improve UI/UX 

---

## Author

Biljana
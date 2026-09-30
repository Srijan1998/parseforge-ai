# ParseForge AI

**ParseForge AI** is an intelligent document processing platform for ingesting documents, extracting text from both digital and scanned PDFs, and building structured AI-powered document understanding workflows.

The project combines a **Spring Boot backend** with a **Python/FastAPI AI service**, using asynchronous processing, object storage, caching, OCR, and local language models.

> **Project Status:** Active development

---

## Overview

Document processing becomes challenging when documents arrive in different formats and contain a mixture of embedded text, scanned pages, and unstructured information.

ParseForge is designed around a processing pipeline that can:

- Upload and persist documents
- Detect duplicate content using SHA-256 hashing
- Store document binaries in S3-compatible object storage
- Process documents asynchronously
- Extract embedded PDF text
- Detect when extracted text is insufficient
- Fall back to OCR for scanned documents
- Persist extraction results and processing metadata
- Integrate local language models for structured document understanding

The longer-term goal is to evolve ParseForge into a complete intelligent document processing system supporting classification, structured extraction, semantic search, RAG, confidence-based model routing, and human review.

---

## Architecture

```text
                         Client
                           │
                           ▼
                  ┌─────────────────┐
                  │   Spring Boot   │
                  │    REST API     │
                  └────────┬────────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
         PostgreSQL      MinIO        Redis
         Metadata &      Object       Queue &
         Job State       Storage      Cache
              │                         │
              │                         ▼
              │                Processing Worker
              │                         │
              │                         ▼
              │                  PDFBox Extraction
              │                         │
              │                  Text sufficient?
              │                    /          \
              │                  Yes           No
              │                   │             │
              │                   │             ▼
              │                   │       FastAPI AI Service
              │                   │             │
              │                   │       PyMuPDF + Tesseract
              │                   │             │
              │                   └──────┬──────┘
              │                          │
              └──────────────────────────▼
                               Extraction Results
```

The backend owns workflow state and persistence, while the Python service provides AI/ML-oriented processing capabilities.

---

## Technology Stack

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA / Hibernate
- Flyway
- PostgreSQL
- Redis
- MinIO
- Apache PDFBox
- Maven

### AI Service

- Python
- FastAPI
- PyMuPDF
- Tesseract OCR
- pytesseract
- Pillow
- Pydantic
- Ollama
- Qwen

### Infrastructure

- Docker
- Docker Compose
- S3-compatible object storage
- Redis-backed asynchronous processing

---

## Current Processing Pipeline

### 1. Document Upload

Documents are uploaded through the Spring Boot REST API.

```text
POST /api/documents/upload
```

ParseForge validates the uploaded document before processing.

Current restrictions include:

- PDF documents
- Maximum file size of 10 MB

---

### 2. Content Hashing and Deduplication

ParseForge computes a **SHA-256 hash** for each uploaded document.

The hash is used to identify previously uploaded content.

```text
Upload
   │
   ▼
SHA-256
   │
   ▼
Redis lookup
   │
   ├── Hit ───────────────► Existing document
   │
   └── Miss
        │
        ▼
   PostgreSQL lookup
        │
        ├── Found ────────► Repopulate Redis
        │
        └── Not found
             │
             ▼
           Upload
```

Redis acts as an acceleration layer while PostgreSQL remains the source of truth.

---

### 3. Object Storage

Actual document binaries are stored in **MinIO**, providing an S3-compatible storage abstraction.

PostgreSQL stores document metadata and the corresponding object key rather than storing document binaries directly.

---

### 4. Asynchronous Processing

New documents create a processing job.

```text
Document
   │
   ▼
Processing Job
   │
   ▼
Redis Queue
   │
   ▼
Background Worker
```

Processing jobs maintain lifecycle information including:

- Status
- Attempt count
- Start time
- Completion time
- Error code
- Error message

Current job states include:

```text
PENDING
PROCESSING
COMPLETED
FAILED
```

Failures are classified as retryable or non-retryable, with bounded retry behavior.

---

## Text Extraction

ParseForge uses a hybrid extraction strategy.

### Embedded Text

Apache PDFBox is used first to extract text already embedded within a PDF.

```text
PDF
 │
 ▼
PDFBox
 │
 ▼
Text Quality Check
```

Returning text is not automatically considered successful extraction. ParseForge performs a basic quality check to determine whether the extracted content appears usable.

---

### OCR Fallback

If embedded text is missing or insufficient, processing falls back to OCR.

```text
PDFBox
   │
   ▼
Insufficient text
   │
   ▼
Spring Boot
   │
   │ HTTP multipart
   ▼
FastAPI
   │
   ▼
PyMuPDF
   │
   ▼
PDF pages → Images
   │
   ▼
Tesseract OCR
   │
   ▼
Extracted Text
```

The extraction method is persisted alongside the result:

```text
EMBEDDED_TEXT
OCR
```

This allows downstream processing to understand how the document content was obtained.

---

## Spring Boot ↔ FastAPI Integration

The Spring Boot application communicates with the AI service over HTTP.

```text
Spring Boot :8080
       │
       │ HTTP
       ▼
FastAPI :8000
```

The FastAPI service is deliberately separated from workflow persistence.

Spring Boot owns:

- Document state
- Processing jobs
- Persistence
- Queue orchestration
- Object storage coordination

FastAPI owns:

- OCR
- AI/ML inference
- Future embedding generation
- Future model-based extraction

This keeps AI-specific dependencies isolated from the core backend.

---

## Local Language Models

ParseForge is being extended with local small language model inference using **Ollama**.

The initial model experiments use lightweight Qwen models suitable for local development.

The planned structured extraction flow is:

```text
Extracted Text
      │
      ▼
 Local SLM
      │
      ▼
Structured JSON
      │
      ▼
Pydantic Validation
      │
      ▼
Persisted Structured Data
```

The goal is to evaluate smaller models first and escalate to larger models only when necessary.

---

## Running Locally

### Prerequisites

Install:

- Java 21
- Docker
- Docker Compose
- Python
- Tesseract OCR
- Ollama

---

### Start Infrastructure

From the project root:

```cmd
docker compose up -d
```

This starts:

- PostgreSQL
- Redis
- MinIO

MinIO Console:

```text
http://localhost:9001
```

---

### Start the Spring Boot Backend

```cmd
mvnw.cmd spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

---

### Start the AI Service

Navigate to:

```cmd
cd ai-service
```

Create a virtual environment:

```cmd
python -m venv .venv
```

Activate it:

```cmd
.venv\Scripts\activate
```

Install dependencies:

```cmd
pip install -r requirements.txt
```

Run FastAPI:

```cmd
uvicorn app.main:app --reload --port 8000
```

The AI service runs on:

```text
http://localhost:8000
```

Health check:

```text
GET /health
```

---

### Upload a Document

Example:

```cmd
curl -X POST http://localhost:8080/api/documents/upload -F "file=@C:\path\to\document.pdf"
```

The request returns after the document has been accepted. Processing continues asynchronously.

---

## Database Migrations

Database schema changes are managed through **Flyway**.

Hibernate is configured to validate the schema rather than create it:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

This keeps database evolution explicit and reproducible.

---

## Design Principles

### PostgreSQL is the Source of Truth

Redis is used for caching and queueing but does not own durable document state.

### Store Files Outside the Database

Document binaries belong in object storage. PostgreSQL stores metadata and references.

### Keep AI Behind a Service Boundary

Spring Boot does not directly depend on Python ML libraries.

### Treat Model Output as Untrusted Input

AI-generated structured data must be validated before being accepted.

### Prefer Asynchronous Processing

Expensive document processing should not block the upload request.

### Optimize After Understanding the System

The project intentionally starts with straightforward implementations before introducing distributed-system complexity.

---

## Roadmap

### Document Intelligence

- [x] PDF upload
- [x] Object storage
- [x] SHA-256 deduplication
- [x] Asynchronous processing
- [x] Embedded PDF text extraction
- [x] OCR fallback
- [x] Processing lifecycle tracking
- [x] Failure classification and bounded retries
- [ ] Document classification
- [ ] Schema-based structured extraction
- [ ] Confidence scoring
- [ ] Human review workflow

### Generative AI

- [ ] Local SLM inference
- [ ] Structured model outputs
- [ ] Model routing
- [ ] LangChain integration
- [ ] Embedding generation
- [ ] Document chunking
- [ ] pgvector
- [ ] Semantic search
- [ ] Retrieval-Augmented Generation (RAG)
- [ ] Reranking
- [ ] RAG evaluation
- [ ] Local-model vs hosted-model evaluation

### Backend & Infrastructure

- [ ] Kafka-based event processing
- [ ] Reliable queue/outbox pattern
- [ ] Idempotent processing
- [ ] Distributed processing
- [ ] Rate limiting
- [ ] Dockerize AI service
- [ ] OpenTelemetry
- [ ] Prometheus
- [ ] Grafana
- [ ] Kubernetes
- [ ] CI/CD

---

## Planned RAG Architecture

```text
Document
   │
   ▼
Text Extraction
   │
   ▼
Chunking
   │
   ▼
Embedding Model
   │
   ▼
PostgreSQL + pgvector
   │
   │
   │       User Question
   │             │
   │             ▼
   │      Query Embedding
   │             │
   └─────────────▼
       Vector Retrieval
             │
             ▼
        Relevant Chunks
             │
             ▼
          LLM / SLM
             │
             ▼
       Grounded Answer
```

---

## Future Model Routing

Rather than sending every document to the largest available model, ParseForge aims to make model selection confidence- and cost-aware.

```text
Document
   │
   ▼
Rules / Small Model
   │
   ▼
Confident?
  /     \
Yes      No
 │        │
 ▼        ▼
Result   Larger Model
             │
             ▼
         Confident?
          /     \
        Yes      No
         │        │
         ▼        ▼
       Result   Hosted LLM
```

This enables experimentation with the trade-offs between accuracy, latency, compute requirements, and inference cost.

---

## Why ParseForge?

ParseForge is being built as both an intelligent document processing platform and an exploration of production-oriented backend and AI engineering.

Rather than treating OCR, LLMs, RAG, databases, queues, and model inference as isolated demos, the project brings them together into one evolving system where architectural decisions can be measured and understood.

---

## License

This project is currently intended for learning and experimentation.

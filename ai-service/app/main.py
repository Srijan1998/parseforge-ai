from fastapi import FastAPI, UploadFile, File

from app.ocr.tesseract_ocr import TesseractOcrService

app = FastAPI(title="ParseForge AI Service")

ocr_service = TesseractOcrService()

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/ocr")
async def extract_text(file: UploadFile = File(...)):
    content = await file.read()

    text = ocr_service.extract(content)

    return {
        "text": text,
        "method": "OCR",
        "size": len(content)
    }
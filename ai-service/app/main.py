from fastapi import FastAPI, UploadFile, File

app = FastAPI(title="ParseForge AI Service")

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/ocr")
async def extract_text(file: UploadFile = File(...)):
    content = await file.read()

    return {
        "text": "Temporary OCR response",
        "method": "OCR",
        "size": len(content)
    }
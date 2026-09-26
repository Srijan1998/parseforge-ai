import io

import fitz
import pytesseract
from PIL import Image


class TesseractOcrService:

    def extract(self, pdf_bytes: bytes) -> str:
        document = fitz.open(
            stream=pdf_bytes,
            filetype="pdf"
        )

        pages = []

        try:
            for page_number, page in enumerate(document):
                pixmap = page.get_pixmap(dpi=300)

                image = Image.open(
                    io.BytesIO(pixmap.tobytes("png"))
                )

                text = pytesseract.image_to_string(
                    image,
                    lang="eng"
                )

                pages.append(text.strip())

        finally:
            document.close()

        return "\n\n".join(pages)
package com.jj.pdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

class PdfToTextServiceImpl implements PdfToTextService {

    String toText(final File pdfFile) {
        final PDDocument pdfDocument = PDDocument.load(pdfFile)
        final PDFTextStripper pdfTextStripper = new PDFTextStripper()
        final String pdfText = pdfTextStripper.getText(pdfDocument)

        pdfDocument.close()

        return pdfText
    }

}

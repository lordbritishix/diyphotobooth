package com.diyphotobooth.lordbritishix.jobprocessor.printer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.PrinterResolution;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MontagePrinter {
    public void print(Path filename) throws PrintException, IOException {
        PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
        attributeSet.add(new MediaPrintableArea(0, 0, 4, 6, MediaPrintableArea.INCH));
        attributeSet.add(new PrinterResolution(300, 300, PrinterResolution.DPI));
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();

        if (printService == null) {
            throw new PrintException("No printer services available");
        }

        DocPrintJob job = printService.createPrintJob();
        try (InputStream is = new FileInputStream(filename.toFile())) {
            Doc doc = new SimpleDoc(is, DocFlavor.INPUT_STREAM.PNG, null);
            job.print(doc, attributeSet);
        }
    }
}

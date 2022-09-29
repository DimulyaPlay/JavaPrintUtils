import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.annotations.PdfRubberStampAnnotation;
import com.spire.pdf.annotations.appearance.PdfAppearance;
import com.spire.pdf.graphics.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import py4j.GatewayServer;

import javax.print.DocPrintJob;
import javax.print.PrintService;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

class JPrint {

    static void printToPrinter(String filepath, String printerName, String jobName) throws PrinterException {
        PrintService[] service = PrinterJob.lookupPrintServices();
        DocPrintJob docPrintJob = null;
        int count = service.length;
        for (int i = 0; i < count; i++) {
            if (service[i].getName().equalsIgnoreCase(printerName)) {
                docPrintJob = service[i].createPrintJob();
                i = count;
            }
        }

        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat loPageFormat  = job.defaultPage();
        Paper loPaper = loPageFormat.getPaper();
        loPaper.setImageableArea(0,0,loPageFormat.getWidth(),loPageFormat.getHeight());
        if (docPrintJob != null) {
            job.setPrintService(docPrintJob.getPrintService());
        }
        job.setJobName(jobName);
        loPageFormat.setPaper(loPaper);
        com.spire.pdf.PdfDocument document = new com.spire.pdf.PdfDocument(filepath);
        job.setPrintable(document, loPageFormat);
        job.print();
        document.close();
    }

    static String generatePDFFromImage(String filename) throws IOException {
        PdfWriter writer = new PdfWriter(filename+".pdf");
        com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
        pdf.addNewPage(PageSize.A4);
        Document document = new Document(pdf);
        ImageData data = ImageDataFactory.create(filename);
        Image image = new Image(data);
        boolean b = image.getImageWidth() > image.getImageHeight();
        if (b) {
            image.setRotationAngle(4.71239);
        }
        image.setAutoScale(true);
        document.add(image);
        document.close();
        pdf.close();
        writer.close();
        return filename+".pdf";
    }

    static String extractTextFromPdf(String filename) throws IOException {
        File file = new File(filename);
        PDDocument document = Loader.loadPDF(file);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String text = pdfStripper.getText(document);
        document.close();
        text = text.replace("\n", "").replace("\r", "");
        return text;
    }

    static String concatenatePdfs(String[] filenames, boolean isDel) throws IOException {
        String newFilename = filenames[0]+"+concatenated.pdf";
        PDFMergerUtility PDFMerger = new PDFMergerUtility();
        PDFMerger.setDestinationFileName(newFilename);
        for (String filename : filenames) {
            File file = new File(filename);
            PDFMerger.addSource(file);
        }

        PDFMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

        if (isDel){
            for (String filename : filenames) {
                File file = new File(filename);
                file.delete();
            }
        }
        return newFilename;
    }

    static String addStamp(String filename, String numAppeal, String numDoc) {
        String newFilename = filename + "_stamp.pdf";
        com.spire.pdf.PdfDocument document = new com.spire.pdf.PdfDocument();
        document.loadFromFile(filename);
        PdfPageBase page = document.getPages().get(0);
        PdfTemplate template = new PdfTemplate(125, 55);
        PdfTrueTypeFont font = new PdfTrueTypeFont(new Font("Times New Roman", Font.BOLD,8), true);
        PdfSolidBrush brush = new PdfSolidBrush(new PdfRGBColor(128,128, 128));
        PdfPen pen = new PdfPen(brush);
        Rectangle2D rectangle = new Rectangle2D.Float();
        rectangle.setFrame(new Point2D.Float(5, 5), template.getSize());
        String s1 = numAppeal;
        String s2 = numDoc;
        template.getGraphics().drawString(s1, font, brush, new Point2D.Float(10, 10));
        template.getGraphics().drawString(s2, font, brush, new Point2D.Float(10, 20));
        PdfRubberStampAnnotation stamp = new PdfRubberStampAnnotation(rectangle);
        PdfAppearance appearance = new PdfAppearance(stamp);
        appearance.setNormal(template);
        stamp.setAppearance(appearance);
        page.getAnnotationsWidget().add(stamp);
        document.saveToFile(filename, FileFormat.PDF);
        document.close();
        return filename;
    }

    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new JPrint());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }

}
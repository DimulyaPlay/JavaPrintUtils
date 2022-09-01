import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import py4j.GatewayServer;

import javax.print.DocPrintJob;
import javax.print.PrintService;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

class JPrint {

    static void printToPrinter(String filepath, String printerName, String jobName) throws IOException, PrinterException {
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
        assert docPrintJob != null;
        job.setPrintService(docPrintJob.getPrintService());
        job.setJobName(jobName);
        loPageFormat.setPaper(loPaper);
        job.setPrintable(new com.spire.pdf.PdfDocument(filepath), loPageFormat);
        job.print();
        System.gc();
    }

    static String generatePDFFromImage(String filename) throws IOException {
        PdfWriter writer = new PdfWriter(filename+".pdf");
        PdfDocument pdf = new PdfDocument(writer);
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
        System.gc();
        return filename+".pdf";
    }

    static String extractTextFromPdf(String filename) throws IOException {
        File file = new File(filename);
        PDDocument document = Loader.loadPDF(file);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String text = pdfStripper.getText(document);
        document.close();
        text = text.replace("\n", "").replace("\r", "");
        System.gc();
        return text;
    }

    static String concatenateTwoPdfs(String filename1,String filename2) throws IOException {
        String newFilename = filename1+"+prot.pdf";
        PDFMergerUtility PDFMerger = new PDFMergerUtility();
        PDFMerger.setDestinationFileName(newFilename);
        File file1 = new File(filename1);
        File file2 = new File(filename2);
        PDFMerger.addSource(file1);
        PDFMerger.addSource(file2);
        PDFMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        file1.delete();
        file2.delete();
        System.gc();
        return newFilename;
    }

    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new JPrint());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }

}
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
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import py4j.GatewayServer;

import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.Sides;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;

class JPrint {

    static void printToPrinter(String filepath, String printerName, String jobName, int duplexMode, int startPageRange, int endPageRange) throws PrinterException {
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
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        switch (duplexMode){
            case (0):
                aset.add(Sides.ONE_SIDED);
                break;
            case (1):
                aset.add(Sides.TWO_SIDED_LONG_EDGE);
                break;
            case (2):
                aset.add(Sides.TWO_SIDED_SHORT_EDGE);
                break;
            default:
                break;
        }
        aset.add(new PageRanges(startPageRange,endPageRange));
        job.setJobName(jobName);
        loPageFormat.setPaper(loPaper);
        com.spire.pdf.PdfDocument document = new com.spire.pdf.PdfDocument(filepath);
        job.setPrintable(document, loPageFormat);
        job.print(aset);
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

    static String extractTextFromPdf(String filename, boolean ocr) throws IOException, TesseractException, URISyntaxException {
        String text;
        File file = new File(filename);
        if (ocr) {
            ITesseract tess = new Tesseract();
            CodeSource codeSource = JPrint.class.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            String jarDir = jarFile.getParentFile().getPath();
            tess.setDatapath(jarDir);
            tess.setLanguage("rus");
            PDDocument document = Loader.loadPDF(file);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.GRAY);
            text = tess.doOCR(bim);
        } else {
            PDDocument document = Loader.loadPDF(file);
            PDFTextStripper pdfStripper = new PDFTextStripper();
            text = pdfStripper.getText(document);
            document.close();
            text = text.replace("\n", "").replace("\r", "");
        }
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
        com.spire.pdf.PdfDocument document = new com.spire.pdf.PdfDocument();
        document.loadFromFile(filename);
        PdfPageBase page = document.getPages().get(0);
        PdfTemplate template = new PdfTemplate(600, 70);
        PdfTrueTypeFont font = new PdfTrueTypeFont(new Font("Times New Roman", Font.BOLD,8), true);
        PdfSolidBrush brush = new PdfSolidBrush(new PdfRGBColor(80,80, 80));
        PdfPen pen = new PdfPen(brush);
        Rectangle2D rectangle = new Rectangle2D.Float();
        rectangle.setFrame(new Point2D.Float(5, 5), template.getSize());
        template.getGraphics().drawString(numAppeal, font, brush, new Point2D.Float(480, 10));
        template.getGraphics().drawString(numDoc, font, brush, new Point2D.Float(480, 20));
        if ("?????????".equals(numDoc)){
            PdfTrueTypeFont font2 = new PdfTrueTypeFont(new Font("Times New Roman", Font.BOLD,12), true);
            template.getGraphics().drawString("         ???????? ? \n??????????? ????", font2, brush, new Point2D.Float(410, 35));
        }
        PdfRubberStampAnnotation stamp = new PdfRubberStampAnnotation(rectangle);
        PdfAppearance appearance = new PdfAppearance(stamp);
        appearance.setNormal(template);
        stamp.setAppearance(appearance);
        page.getAnnotationsWidget().add(stamp);
        document.saveToFile(filename, FileFormat.PDF);
        document.close();
        return filename;
    }

    public static void main(String[] args) throws URISyntaxException {
        GatewayServer gatewayServer = new GatewayServer(new JPrint());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
        CodeSource codeSource = JPrint.class.getProtectionDomain().getCodeSource();
        File jarFile = new File(codeSource.getLocation().toURI().getPath());
        String jarDir = jarFile.getParentFile().getPath();
        System.out.println(jarDir);
    }

}
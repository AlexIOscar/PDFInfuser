package testpkg;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.util.Matrix;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

public class Starter {

    public static void main(String[] args) throws IOException {

        PDDocument doc = PDDocument.load(new File("TestDraw.pdf"));
        //zero index corresponds to the first page
        int count = doc.getNumberOfPages();
        for (int i = 0; i < count; i++) {
            addWatermarks(doc, i, "ORDER: 123456 Placed by: Ivanov I.O. Printed by: Tolstokulakov A.");
        }

        PDPage page = doc.getPage(0);
        Iterator<PDStream> iter = page.getContentStreams();
        InputStream inpStr = page.getContents();
        System.out.println(new String(inpStr.readAllBytes()));
        PDStream stream = null;
        while (iter.hasNext()){
            stream = iter.next();
        }

        assert stream != null;
        InputStream istr = stream.createInputStream();
        byte[] bytes = istr.readAllBytes();
        String content = new String(bytes);
        //System.out.println(content);
        OutputStream ostr = stream.createOutputStream();
        ostr.write(content.getBytes());
        ostr.close();
        doc.save(new File("TestDraw-2.pdf"));

        new ProcessBuilder("C:\\Program Files\\Adobe\\Acrobat DC\\Acrobat\\Acrobat.exe",
                "C:\\IntellijProj\\PDFInfuser\\TestDraw-2.pdf").start();
        doc.close();
    }

    private static void addWatermarks(PDDocument doc, int pageIndex, String textToBake) throws IOException {
        PDPage page = doc.getPage(pageIndex);

        //checking the metadata
        PDMetadata meta = page.getMetadata();
        if (meta != null) {
            String metaString = new String(meta.exportXMPMetadata().readAllBytes());
            if (metaString.equals("PDFInfuserMetadata")) {
                System.out.println("Already watermarked!");
            }
        }

        COSStream cos = new COSStream();
        OutputStream outstr = cos.createOutputStream();
        outstr.write("PDFInfuserMetadata".getBytes());
        outstr.close();
        page.setMetadata(new PDMetadata(cos));

        PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, false);
        contentStream.addComment("This is programming added comment");
        //common tunes
        contentStream.setRenderingMode(RenderingMode.FILL_STROKE);
        PDFont font = PDType1Font.HELVETICA_OBLIQUE;
        contentStream.setLineWidth(0.12f);
        contentStream.setNonStrokingColor(Color.BLUE.darker());
        contentStream.setStrokingColor(Color.WHITE);
        contentStream.setFont(font, 12);

        contentStream.beginText();

        bakeWatermark(textToBake, page, contentStream);

        contentStream.endText();
        contentStream.close();
    }

    /**
     * A method that moves and rotates the "carriage", and writes text at position
     *
     * @param text          text to writing
     * @param page          page to writing
     * @param contentStream contentStream including new text
     * @throws IOException IOException
     */
    private static void bakeWatermark(String text, PDPage page, PDPageContentStream contentStream) throws IOException {
        Matrix matrix = new Matrix();
        contentStream.newLineAtOffset(5, 5);
        contentStream.showText(text);

        transformMatrix(matrix, page.getMediaBox().getWidth(), 0, 90);
        contentStream.setTextMatrix(matrix);
        contentStream.newLineAtOffset(5, 5);
        contentStream.showText(text);

        transformMatrix(matrix, page.getMediaBox().getHeight(), 0, 90);
        contentStream.setTextMatrix(matrix);
        contentStream.newLineAtOffset(5, 5);
        contentStream.showText(text);

        transformMatrix(matrix, page.getMediaBox().getWidth(), 0, 90);
        contentStream.setTextMatrix(matrix);
        contentStream.newLineAtOffset(5, 5);
        contentStream.showText(text);
    }

    /**
     * Text matrix transforming
     *
     * @param matrix Matrix that transforms
     * @param x      x translation
     * @param y      y translation
     * @param angle  angle of rotation
     */
    private static void transformMatrix(Matrix matrix, float x, float y, double angle) {
        matrix.translate(x, y);
        matrix.rotate(Math.toRadians(angle));
    }
}
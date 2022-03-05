package pdfinfuser.core;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.util.Matrix;

import java.awt.*;
import java.io.*;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static testpkg.Starter.logger;

public class TextInfuser {

    PDDocument doc;

    private final PDType0Font font;

    public TextInfuser(PDDocument doc) throws IOException {
        this.doc = doc;
        FileInputStream helvFis = new FileInputStream(new File("C:\\OEMZ\\Production manager\\Java\\AGHelvetica.ttf"));
        this.font = PDType0Font.load(this.doc, helvFis, false);
        helvFis.close();
    }

    /**
     * The method places input text into each corner of each page of document doc, along of page edge
     * @param text text inject to
     * @return PDDocument with changes
     * @throws IOException I/O Exception
     */
    public PDDocument injectText(String text) throws IOException {
        //zero index corresponds to the first page
        // чекаем метаданные нулевой страницы, если документ уже ранее "прошивался", необходимо заменить в нем ранее
        // внесенные ватермарки новыми, о чем сообщаем через флаг
        boolean alreadyMarked = false;
        PDPage page = doc.getPage(0);
        PDMetadata meta = page.getMetadata();
        if (meta != null) {
            String metaString = new String(IOUtils.toByteArray(meta.exportXMPMetadata()));
            if (metaString.equals("PDFInfuserMetadata")) {
                alreadyMarked = true;
                logger.info("The document already watermarked, mark will be replaced");
            }
        }

        int count = doc.getNumberOfPages();
        for (int i = 0; i < count; i++) {
            if (alreadyMarked) {
                logger.trace("Replacing watermark on page {}", i);
                replaceWM(doc, i, text);
            } else {
                logger.trace("Adding watermark on page {}", i);
                addWatermarks(doc, i, text);
            }
        }
        return doc;
    }

    /**
     * Do injectText-business with the page if the page watermarked already
     * @param doc processing document
     * @param pageIndex page inject to
     * @param replacingText text watermarked to (via replacing)
     * @throws IOException I/O Exception
     */
    private void replaceWM(PDDocument doc, int pageIndex, String replacingText) throws IOException {
        PDPage page = doc.getPage(pageIndex);
        Iterator<PDStream> contStrs = page.getContentStreams();
        while (contStrs.hasNext()) {
            PDStream contStream = contStrs.next();
            InputStream istr = contStream.createInputStream();
            String content = new String(IOUtils.toByteArray(istr));
            Pattern pattern = Pattern.compile("^%This is PDFInfuser technical note. Please don't remove it for the " +
                    "God's sake\\n");
            Matcher matcher = pattern.matcher(content);

            if (matcher.find()) {
                //System.out.println(content);
                String[] splitted = content.split("<[0123456789ABCDEF]+>");
                int parts = splitted.length;

                OutputStream ostr = contStream.createOutputStream();
                for (int i = 0; i < parts; i++) {
                    ostr.write(splitted[i].getBytes());
                    if (i < parts - 1) {
                        COSWriter.writeString(font.encode(replacingText), ostr);
                    }
                }
                ostr.close();
            }
        }
    }

    /**
     * Do injectText-business with the page if the page have not watermarked yet
     * @param doc processing document
     * @param pageIndex page inject to
     * @param textToBake text watermarked to
     * @throws IOException I/O Exception
     */
    private void addWatermarks(PDDocument doc, int pageIndex, String textToBake) throws IOException {
        PDPage page = doc.getPage(pageIndex);

        //set metadata to page
        COSStream cos = new COSStream();
        OutputStream outstr = cos.createOutputStream();
        outstr.write("PDFInfuserMetadata".getBytes());
        outstr.close();
        page.setMetadata(new PDMetadata(cos));

        PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND,
                false, true);
        contentStream.addComment("This is PDFInfuser technical note. Please don't remove it for the God's sake");
        //common tunes
        contentStream.setRenderingMode(RenderingMode.FILL_STROKE);

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

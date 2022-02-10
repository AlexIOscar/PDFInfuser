package testpkg;

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
import pdfinfuser.core.TextInfuser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

public class Starter {

    public static void main(String[] args) throws IOException {

        PDDocument doc = PDDocument.load(new File("TestDraw.pdf"));

        TextInfuser.injectText(doc, "ORDER: 123456 Placed by: Ivanov I.O. Printed by: Tolstokulakovv A.");

        doc.save(new File("TestDraw-2.pdf"));
        doc.close();

        new ProcessBuilder("C:\\Program Files\\Adobe\\Acrobat DC\\Acrobat\\Acrobat.exe",
                "C:\\IntellijProj\\PDFInfuser\\TestDraw-2.pdf").start();
    }
}
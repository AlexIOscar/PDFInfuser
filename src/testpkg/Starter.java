package testpkg;

import org.apache.pdfbox.pdmodel.PDDocument;
import pdfinfuser.core.TextInfuser;

import java.io.File;
import java.io.IOException;

public class Starter {

    public static void main(String[] args) throws IOException {

        PDDocument doc = PDDocument.load(new File("TestDraw.pdf"));

        //TextInfuser.injectText(doc, "ORDER: 123456 Placed by: Иванов (I.O.) Printed by: Tolstokulakov A.V Я");
        TextInfuser.injectText(doc, "Съешь еще этих чудных французских булок да выпей чаю");

        doc.save(new File("TestDraw-2.pdf"));
        doc.close();

        /*
        new ProcessBuilder("C:\\Program Files\\Adobe\\Acrobat DC\\Acrobat\\Acrobat.exe",
                "C:\\IntellijProj\\PDFInfuser\\TestDraw-2.pdf").start();
         */
    }
}
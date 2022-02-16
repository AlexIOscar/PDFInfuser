package testpkg;

import org.apache.pdfbox.pdmodel.PDDocument;
import pdfinfuser.core.TextInfuser;
import pdfinfuser.interaction.Archiver;
import pdfinfuser.interaction.DBInteractor;
import pdfinfuseraux.TextProcessor;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class Starter {

    public static void main(String[] args) throws IOException {

        String originName = "C:\\IntellijProj\\PDFInfuser\\TestDraw.pdf";

        PDDocument doc = PDDocument.load(new File(originName));
        String wmText = "Ёсли тёкст (тест служебных символов) №~!@#$%^&*()_+|}{:?><`-=][;'/.,!;%:?*()_+,/-=.";

        //replacing chars due to helvetica does not contain some glyphs
        wmText = TextProcessor.replaceChars(wmText);

        TextInfuser.injectText(doc, wmText);
        File outPDF = new File(originName.replaceAll(".pdf", "-2.pdf"));
        File out7z = new File(originName.replaceAll(".pdf", ".7z"));
        doc.save(outPDF);
        doc.close();

        Archiver.archiveFile(outPDF.getAbsolutePath(), out7z.getAbsolutePath());
        System.out.println(outPDF.delete());

        try {
            DBInteractor dbi = new DBInteractor();
            if (dbi.con != null) {
                //dbi.setBlob(45765, out7z);
                dbi.getBlob(45765);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        /*
        new ProcessBuilder("C:\\Program Files\\Adobe\\Acrobat DC\\Acrobat\\Acrobat.exe",
                "C:\\IntellijProj\\PDFInfuser\\TestDraw-2.pdf").start();
         */
    }
}
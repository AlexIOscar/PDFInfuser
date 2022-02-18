package testpkg;

import org.apache.pdfbox.pdmodel.PDDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pdfinfuser.core.TextInfuser;
import pdfinfuser.interaction.Archiver;
import pdfinfuser.interaction.DBInteractor;
import pdfinfuseraux.TextProcessor;

import java.io.*;
import java.sql.SQLException;

public class Starter {

    private static final Logger logger
            = LoggerFactory.getLogger(Starter.class);

    public static void main(String[] args) {
        File origin;

        if (args.length != 0) {
            origin = new File(args[0]);
        } else {
            origin = new File("C:\\Users\\Aleksey\\Downloads\\Telegram Desktop\\ListOfFiles.txt");
        }

        if (!origin.exists()) {
            logger.error("No such file at placed address or input was empty");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(origin)))) {
            while (true) {
                String strFileID = reader.readLine();
                String filePath = reader.readLine();
                String wmText = reader.readLine();
                if (strFileID == null || filePath == null || wmText == null) {
                    break;
                }
                strFileID = strFileID.replaceAll("ID_FILE = ", "");
                filePath = filePath.replaceAll("FILE_PATH = ", "");
                wmText = wmText.replaceAll("INPUTED_TEXT = ", "");

                File inPDF = new File(filePath);
                PDDocument doc = PDDocument.load(inPDF);

                //replacing chars due to helvetica does not contain some glyphs
                wmText = TextProcessor.replaceChars(wmText);

                TextInfuser.injectText(doc, wmText);
                File outPDF = new File(filePath.replaceAll("\\.pdf", "-m.pdf"));
                File out7z = new File(filePath.replaceAll("\\.pdf", ".7z"));
                doc.save(outPDF);
                doc.close();

                Archiver.archiveFile(outPDF.getAbsolutePath(), out7z.getAbsolutePath());

                System.out.println("input PDF deleted:" + inPDF.delete());
                System.out.println("output PDF deleted:" + outPDF.delete());

                try {
                    DBInteractor dbi = new DBInteractor();
                    if (dbi.con != null) {
                        dbi.setBlob(Integer.parseInt(strFileID), out7z);
                    }
                    System.out.println("temporary arch deleted:" + out7z.delete());
                } catch (SQLException | NumberFormatException throwables) {
                    throwables.printStackTrace();
                }
            }
        } catch (IOException ioE) {
            logger.error("Exception", ioE);
        }
        System.out.println("origin file deleted:" + origin.delete());
    }
}
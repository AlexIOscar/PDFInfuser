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

import ch.qos.logback.classic.util.ContextInitializer;

public class Starter {

    public static final Logger logger;

    static {
        rewriteLogOutput();
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "C:\\OEMZ\\Production manager\\Java\\logback.xml");
        logger = LoggerFactory.getLogger("pdfinfuser.Starter");
    }

    /**
     * entry point
     * @param args the defined call signature is:
     *             arg[0] have to contain absolute path to text file with structure (starts with 1):
     *             1*n line: "ID_FILE = XXX", where XXX is ID in DB
     *             2*n line: "FILE_PATH = XXX", where XXX is absolute path to .pdf file
     *             3*n line: "INPUTED_TEXT = XXX", where XXX is text to placed in input .pdf file
     *             The ListOFFiles.txt have to contain 3*n lines, or error "Wrong input file structure" will be logged
     */
    public static void main(String[] args) {
        File origin;
        logger.info("Exec arg 0: {}", args[0]);
        if (args.length != 0) {
            origin = new File(args[0]);
        } else {
            origin = new File("C:\\Users\\Aleksey\\AppData\\Local\\Temp\\ProductionManager\\Temp File " +
                    "Storage\\Changing PDF Files\\ListOfFiles.txt");
            logger.error("The arguments list is empty");
        }

        if (!origin.exists()) {
            logger.error("No such file at placed address or input was empty");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(origin), "CP1251"))) {
            while (true) {
                String strFileID = reader.readLine();
                String filePath = reader.readLine();
                String wmText = reader.readLine();
                if (strFileID == null || filePath == null || wmText == null) {
                    logger.error("Wrong input file structure");
                    break;
                }
                strFileID = strFileID.replaceAll("ID_FILE = ", "");
                filePath = filePath.replaceAll("FILE_PATH = ", "");
                wmText = wmText.replaceAll("INPUTED_TEXT = ", "");

                File inPDF = new File(filePath);
                logger.trace("processing document: {}", inPDF.getName());
                PDDocument doc = PDDocument.load(inPDF);

                //replacing chars due to helvetica does not contain some glyphs
                wmText = TextProcessor.replaceChars(wmText);

                TextInfuser textInf = new TextInfuser(doc);

                textInf.injectText(wmText);
                File outPDF = new File(filePath);
                File out7z = new File(filePath.replaceAll("\\.pdf", ".7z"));
                doc.save(outPDF);
                doc.close();

                Archiver.archiveFile(outPDF.getAbsolutePath(), out7z.getAbsolutePath());

                System.out.println("input PDF deleted:" + inPDF.delete());
                //System.out.println("output PDF deleted:" + outPDF.delete());

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

    /**
     * The method defined location of the log-file and the logger configuration .xml
     */
    public static void rewriteLogOutput() {
        File logConfigFile = new File("C:\\OEMZ\\Production manager\\Java\\logback.xml");
        if (logConfigFile.exists()) {
            System.out.println("logger configuration file successfully found");
        }
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(logConfigFile))) {
            String line = br.readLine();

            while (line != null) {
                if (line.matches(".*<file>.*</file>.*")) {
                    String homeAddr = System.getProperty("user.home").replaceAll("\\\\", "\\\\\\\\");
                    String addr = homeAddr + "\\\\PJMServices\\\\PDFInfuser\\\\PDFI.log";
                    line = line.replaceAll("(?<=<file>).*(?=</file>)", addr);
                }
                content.append(line).append("\n");
                line = br.readLine();
            }
        } catch (IOException ex) {
            System.out.println("logger config file exception");
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logConfigFile))) {
            bw.write(content.toString());
        } catch (IOException ex) {
            System.out.println("logger config file exception");
        }
    }
}
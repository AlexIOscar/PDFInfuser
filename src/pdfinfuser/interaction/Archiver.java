package pdfinfuser.interaction;

import pdfinfuseraux.WindowsRegistry;

import java.io.IOException;
import java.util.Arrays;

public class Archiver {
    public static void archiveFile(String input, String output) throws IOException {

        String regAddres = "HKCU\\Software\\OEMZ\\Production Manager";
        String archRoute = WindowsRegistry.readRegistry(regAddres, "Install Directory") + "\\Archivator\\7zG.exe";

        Process proc = new ProcessBuilder(archRoute, "a", output, input, "-mx9").start();

        while (true) {
            if(!proc.isAlive()){
                break;
            }
        }
    }
}
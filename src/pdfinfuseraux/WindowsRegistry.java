package pdfinfuseraux;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * @author Oleg Ryaboy, based on work by Miguel Enriquez, adapted by A.Tolstokulakov
 */
public class WindowsRegistry {

    /**
     * @param location path in the registry
     * @param key      registry key
     * @return registry value or null if not found
     */
    public static String readRegistry(String location, String key) {
        try {
            // Run reg query, then read output with StreamReader (internal class)
            // The key surrounded with double quotes (Tolstokulakov A)
            Process process = Runtime.getRuntime().exec("reg query " + '"' + location + "\" /v " + '"' + key + '"');

            StreamReader reader = new StreamReader(process.getInputStream());
            reader.start();
            process.waitFor();
            reader.join();
            String output = reader.getResult();

            // The weak place in author source: not all versions place \t in output, replaced with "    " (commented
            // by Alex Tolstokulakov)
            // Output has the following format:
            // \n<Version information>\n\n<key>\t<registry type>\t<value>
            if (!output.contains("    ")) {
                return null;
            }

            // Parse out the value
            //parsing changed too (A.Tolstokulakov)
            String[] parsed = output.split(" {4}");
            int index = 0;
            for (String str : parsed) {
                parsed[index] = str.replaceAll("\r\n", "");
                index++;
            }
            return parsed[parsed.length - 1];
        } catch (Exception e) {
            System.out.println("registry reading failure");
            return null;
        }
    }

    static class StreamReader extends Thread {
        private InputStream is;
        private StringWriter sw = new StringWriter();

        public StreamReader(InputStream is) {
            this.is = is;
        }

        public void run() {
            try {
                int c;
                while ((c = is.read()) != -1)
                    sw.write(c);
            } catch (IOException ignored) {
            }
        }

        public String getResult() {
            return sw.toString();
        }
    }
}

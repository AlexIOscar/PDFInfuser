package pdfinfuser.interaction;
import org.apache.pdfbox.io.IOUtils;
import pdfinfuseraux.WindowsRegistry;

import java.io.*;
import java.sql.*;

public class DBInteractor {
    public final Connection con;

    public DBInteractor() throws SQLException {
        String conString = getConnString();

        String regAddres = "HKCU\\Software\\OEMZ\\Production Manager\\Setting\\dsn";
        String userName = WindowsRegistry.readRegistry(regAddres, "UID_FireBird");
        String pass = WindowsRegistry.readRegistry(regAddres, "PWD_2005");

        con = DriverManager.getConnection(conString, userName, pass);
        con.setAutoCommit(false);
    }

    private String getConnString() {
        String regAddres = "HKCU\\Software\\OEMZ\\Production Manager\\Setting\\dsn";

        String serverName = WindowsRegistry.readRegistry(regAddres, "DataSourse_2005");
        String dbName = WindowsRegistry.readRegistry(regAddres, "DATABASE_FireBird");
        String port = "3050";

        return String.format("jdbc:firebirdsql://%s:%s/%s", serverName, port, dbName);
    }

    public void setBlob(int fileID, File archFile) throws SQLException, IOException {
        FileInputStream fis = new FileInputStream(archFile);

        String query = "UPDATE Files_Of_Sheets_On_II SET PDF_FILE = ? WHERE ID_FILE = ?";
        PreparedStatement pstm = con.prepareStatement(query);
        pstm.setBlob(1, fis);
        pstm.setInt(2, fileID);

        System.out.println("rows affected: " + pstm.executeUpdate());
        fis.close();
        con.commit();
    }

    public void getBlob(int listID) throws SQLException, IOException {
        String query = "SELECT PDF_FILE FROM SPRNUMLISTOV WHERE IDNUMLISTA = ?;";
        PreparedStatement pstm = con.prepareStatement(query);
        pstm.setInt(1, listID);
        ResultSet rs = pstm.executeQuery();

        while (rs.next()){
            byte[] content = IOUtils.toByteArray(rs.getBinaryStream(1));
            File outFile = new File("TestOutputArch");
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(content);
            fos.close();
        }
    }
}

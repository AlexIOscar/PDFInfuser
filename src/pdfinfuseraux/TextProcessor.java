package pdfinfuseraux;

public class TextProcessor {
    public static String replaceChars(String inputStr){
        return inputStr.replace('ё', 'е')
                .replace('Ё', 'Е')
                .replace('№', '#');
    }
}

package CSS_Parser;

import Exceptions.InvalidExtensionException;
import Exceptions.InvalidFormatException;

import javax.print.DocFlavor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Emmanuel Olaojo
 * @since 6/1/16
 */
public class Parser {
    private static final String OPEN_COMMENT = "/*";
    private static final String CLOSE_COMMENT = "*/";
    private String fileName;
    private Map<String, CSS> globMap;
    private BufferedReader br;
    private boolean inComment = false;

    public Parser(String fileName, Map<String, CSS> globMap) throws InvalidExtensionException {
        if(!validExt(fileName)) throw new InvalidExtensionException(fileName);

        try {
            this.br = new BufferedReader(new FileReader(new File(fileName)));
        } catch(FileNotFoundException fnf){
            System.err.println("File Not found");
            System.exit(1);
        }

        this.fileName = fileName;
        this.globMap = globMap;
    }

    public boolean validExt(String fileName){
        String[] splitName = fileName.split("\\.");

        return splitName[splitName.length-1].toLowerCase().equals("css");
    }

    public void parse(){
        StringBuilder cssString = new StringBuilder();

        this.br.lines().forEach(line -> {
            if(!this.inComment) {
                line = stripComments(line.trim()).trim();

                if (line.length() > 0) {
                    cssString.append(line);
                }
            }
            else{
                this.inComment = !line.contains(CLOSE_COMMENT);
            }
        });

        try {
            parseCSS(cssString);
        } catch(InvalidFormatException ife){
            System.out.println(ife.getMessage());
            System.exit(1);
        }
    }

    public void parseCSS(StringBuilder cssString) throws InvalidFormatException{
        int openIndex;
        int closeIndex;

        while(cssString.length() > 0){
            openIndex = cssString.indexOf("{");
            closeIndex = cssString.indexOf("}");

            if(openIndex <= 0 || closeIndex < 0 || openIndex > closeIndex) {
//                System.out.println(cssString.toString());
                throw new InvalidFormatException(this.fileName);
            }

            System.out.println(getBlock(cssString, closeIndex));
        }
    }

    public void parseCssBlock(String cssBlock){

    }

    public String getBlock(StringBuilder cssString, int end){
        String block = cssString.substring(0, end + 1);
        cssString.delete(0, end + 1);
        return block;
    }

    public String stripComments(String line){
        int startIndex = line.indexOf(OPEN_COMMENT);
        int endIndex = line.indexOf(CLOSE_COMMENT);
        String cleanLine = line;

        while(startIndex > -1 || endIndex > -1){
            cleanLine = stripComments(cleanLine, startIndex, endIndex);
            startIndex = cleanLine.indexOf(OPEN_COMMENT);
            endIndex = cleanLine.indexOf(CLOSE_COMMENT);
        }

        return cleanLine;
    }

    public String stripComments(String line, int startIndex, int endIndex){
        line = line.trim();

        if(line.length() == 0) return line;

        StringBuilder cleanString = new StringBuilder();
        boolean hasStart = startIndex >= 0;
        boolean hasEnd = endIndex >= 0;

        if(!(hasStart || hasEnd)) return line;

        endIndex = hasEnd ? endIndex: line.length() - 1;
        startIndex = hasStart ? startIndex : 0;

        this.inComment = !hasEnd;

        if(startIndex > endIndex || !hasStart){
            cleanString.append(line.substring(endIndex + 2));
        }
        else if(!hasEnd){
            cleanString.append(line.substring(0, startIndex));
        }
        else{
            cleanString.append(line.substring(0, startIndex))
                    .append(line.substring(endIndex + 2));
        }

        return cleanString.toString();
    }

    public static void main(String[] args) {
        Parser testParser;
        String test = ".prompt-div{\n" +
                "    position: absolute;\n" +
                "    /*width: 100%;*/\n" +
                "    top: 30%;\n" +
                "}";

        try {
            testParser = new Parser("style.css", new HashMap<>());
            //System.out.println(testParser.stripComments(test));
            testParser.parse();
        } catch (InvalidExtensionException iee){
            System.out.println(iee.getMessage());
        }

    }
}

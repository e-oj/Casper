package CSS_Parser;

import Exceptions.InvalidExtensionException;

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

//    public void parse(){
//
//        br.lines().forEach(line -> {
//            line = stripComments(line.trim());
//
//            if(line.size())
//
//        });
//    }
//
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
            System.out.println(testParser.stripComments(test));
        } catch (InvalidExtensionException iee){
            System.out.println(iee.getMessage());
        }

    }
}

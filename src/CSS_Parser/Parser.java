package CSS_Parser;

import Exceptions.InvalidExtensionException;
import Exceptions.InvalidSyntaxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * @author Emmanuel Olaojo
 * @since 6/1/16
 */
public class Parser {
    private static final String OPEN_COMMENT = "/*";
    private static final String CLOSE_COMMENT = "*/";
    private String fileName;
    private Map<String, List<CSS>> globMap;
    private BufferedReader br;
    private boolean inComment = false;

    public Parser(String fileName, Map<String, List<CSS>> globMap) throws InvalidExtensionException {
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

    private boolean validExt(String fileName){
        String[] splitName = fileName.split("\\.");

        return splitName[splitName.length-1].toLowerCase().equals("css");
    }

    public void parse() throws InvalidSyntaxException{
        StringBuilder cssString = new StringBuilder();

        this.br.lines().forEach(line -> {
            if(!this.inComment) {
                line = this.stripComments(line.trim());

                if (line.length() > 0) {
                    cssString.append(line);
                }
            }
            else{
                this.inComment = !line.contains(CLOSE_COMMENT);
            }
        });

        this.parseCSS(cssString);
    }

    private void parseCSS(StringBuilder cssString) throws InvalidSyntaxException {
        int openIndex;
        int closeIndex;

        while(cssString.length() > 0){
            openIndex = cssString.indexOf("{");
            closeIndex = cssString.indexOf("}");

            if(openIndex <= 0 || closeIndex < 0 || openIndex > closeIndex) {
                throw new InvalidSyntaxException(this.fileName);
            }

            this.parseCssBlock(this.getBlock(cssString, closeIndex));
        }
    }

    private void parseCssBlock(String cssBlock) throws InvalidSyntaxException {
        String[] styleParts = cssBlock.split("\\{|}");
        String identifiers = styleParts[0];
        String style = styleParts[1];
        CSS css = createCSS(style);
        String[] iSplit = identifiers.split(",");
        ArrayList<CSS> styleList;

        for(String s: iSplit){
            String key = this.getBaseSelector(s);
            CSS val = new CSS(css, s);

            if(globMap.containsKey(key)){
                globMap.get(key).add(val);
            }
            else{
                styleList = new ArrayList<>();
                styleList.add(val);
                globMap.put(key, styleList);
            }
        }

//        System.out.println(identifiers + css);
    }

    private String getBaseSelector(String identifier) throws InvalidSyntaxException {
        identifier = identifier.trim();
        int start = identifier.length() - 1;
        int end = identifier.length();
        final Set<Character> SELECTORS = new HashSet<>();
        char first = identifier.charAt(0);

        SELECTORS.add('.');
        SELECTORS.add('#');
        SELECTORS.add(' ');
        SELECTORS.add('>');
        SELECTORS.add('+');
        SELECTORS.add('~');

        if(first == '>' || first == '+' || first == '~'){
            System.out.println(identifier);
            throw new InvalidSyntaxException(this.fileName);
        }
        
        while(!SELECTORS.contains(identifier.charAt(start)) && start > 0){
            start--;
        }

        String baseSelector = identifier.substring(start, end).trim();
        char firstChar = baseSelector.charAt(0);

        if(firstChar == '>' || firstChar == '+'|| firstChar == '~')
            return identifier.substring(start + 1, end);

        return baseSelector;
    }

    private CSS createCSS(String styleString) throws InvalidSyntaxException {
        CSS css = new CSS(this.fileName);
        String[] styles = styleString.split(";");

        for(String style: styles){
            String[] styleParts = style.split(":");

            if(styleParts.length > 2)
                throw(new InvalidSyntaxException(this.fileName));

            css.addField(styleParts[0], styleParts[1]);
        }

        return css;
    }

    private String getBlock(StringBuilder cssString, int end) throws InvalidSyntaxException {
        int openCount = 0;
        String block = cssString.substring(0, end + 1);
        cssString.delete(0, end + 1);

        for(int i = 0; i < block.length(); i++){
            if(block.charAt(i) == '{'){
                openCount++;

                if(openCount > 1) throw new InvalidSyntaxException(this.fileName);
            }
        }

        return block;
    }

    private String stripComments(String line){
        int startIndex = line.indexOf(OPEN_COMMENT);
        int endIndex = line.indexOf(CLOSE_COMMENT);
        String cleanLine = line;

        while(startIndex > -1 || endIndex > -1){
            cleanLine = stripComments(cleanLine, startIndex, endIndex);
            startIndex = cleanLine.indexOf(OPEN_COMMENT);
            endIndex = cleanLine.indexOf(CLOSE_COMMENT);
        }

        return cleanLine.trim();
    }

    private String stripComments(String line, int startIndex, int endIndex){
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
        Map<String, List<CSS>> globMap = new HashMap<>();

        try {
            testParser = new Parser("style2.css", globMap);
            testParser.parse();

            globMap.forEach((key, val) -> {
                System.out.println(key);
                System.out.println("_________________________________________________________________");
                val.forEach(System.out::println);
                System.out.println("=====================================================================");
            });

        } catch (Exception iee){
            iee.printStackTrace();
        }
    }
}

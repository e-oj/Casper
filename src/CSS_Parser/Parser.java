package CSS_Parser;

import Exceptions.InvalidExtensionException;
import Exceptions.InvalidSyntaxException;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * CSS parser for Casper. Parses the given CSS file and stores
 * it's selectors/identifiers and values in a Map. This map
 * will be passed into the constructor when the object is created
 * and will be used to determine if multiple CSS style objects
 * are linked to the same element, classname or id.
 *
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
    private final InvalidSyntaxException IVSE;

    /**
     * Constructor takes a file name and the Map that stores the
     * parsed objects. The extension of the provided file must be css.
     * This is the first validity check carried out on the file. After
     * confirming the extension, we attempt to open the file and initialize
     * the object's properties.
     *
     * @param fileName The CSS file to be parsed
     * @param globMap The Map that stores the parsed objects
     *
     * @throws InvalidExtensionException if the file has an Invalid extension.
     */
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
        this.IVSE = new InvalidSyntaxException(this.fileName);
    }

    /**
     * Checks the files extension for validity. The only valid
     * extension is .css
     *
     * @param fileName the filename to check
     *
     * @return true if the extension id valid. false otherwise.
     */
    private boolean validExt(String fileName){
        String[] splitName = fileName.split("\\.");

        return splitName[splitName.length-1].toLowerCase().equals("css");
    }

    /**
     * Reads the file line by line, passes each line through a method
     * that strips out comments, then it adds the line to a StringBuilder
     * (StringBuilder is used, over concatenation, for efficiency). If
     * inComment is true, it checks the current line for the close
     * and if a close comment is present, the line gets added to the
     * StringBuilder, else, the line is seen as a comment and is
     * ignored. When all the lines have been added to the StringBuilder,
     * we run it through the parseCSS method for further processing.
     *
     * @throws InvalidSyntaxException from the parseCSS method
     */
    public void parse() throws InvalidSyntaxException{
        StringBuilder cssString = new StringBuilder();

        this.br.lines().forEach(line -> {
            if(!this.inComment || line.contains(CLOSE_COMMENT)) {
                line = this.stripComments(line.trim());

                if (line.length() > 0) {
                    cssString.append(line);
                }
            }
        });

        this.parseCSS(cssString);
    }

    /**
     * Handles the parsing of the StringBuilder. It gets the
     * positions of the next opening and closing curly braces and
     * checks that they are correctly placed i.e. the index of the opening
     * curly is less than the index of the closing curly. Then it gets
     * the next css style block and passes it to parseCssBlock;
     *
     * @param cssString The StringBuilder containing trimmed, uncommented css
     *
     * @throws InvalidSyntaxException if there's a problem with the braces or
     * from the methods called (getBlock and parseCssBlock)
     */
    private void parseCSS(StringBuilder cssString) throws InvalidSyntaxException {
        int openIndex;
        int closeIndex;
        String block;

        while(cssString.length() > 0){
            openIndex = cssString.indexOf("{");
            closeIndex = cssString.indexOf("}");

            if(openIndex <= 0 || closeIndex < 0 || openIndex > closeIndex) {
                System.out.println(cssString);
                throw this.IVSE;
            }

            block = this.getBlock(cssString, closeIndex);

            if(block.length() > 0)this.parseCssBlock(block);
        }
    }

    /**
     * This method takes in a css block e.g.
     *
     *      .class{color: red;height: 60px;}
     *
     * and parses it into a CSS object, then places it in a map with
     * the associated base selector (see next method) as the key.
     * Things outside the braces are identifiers and inside the
     * braces, you have the style. The rest of the process is
     * pretty straightforward from there just #ReadTheCode
     *
     * @param cssBlock a css String
     *
     * @throws InvalidSyntaxException if illegal certain Illegal characters
     * are encountered or from the methods called (createCSS and getBaseSelector);
     */
    private void parseCssBlock(String cssBlock) throws InvalidSyntaxException {
        String[] styleParts = cssBlock.split("\\{|}");
        String identifiers = styleParts[0];
        String style = styleParts[1];
        CSS css = createCSS(style);
        String[] iSplit = identifiers.split(",");
        ArrayList<CSS> styleList;

        if(identifiers.contains(";")) throw this.IVSE;

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

    /**
     * This method takes in a String of identifiers e.g.
     *
     *      .class1 #id1.hello > #world
     *
     * and returns the base selector.
     *
     * The base selector is the class, element or id that's ultimately
     * affected by the style.
     * In the example case, the base selector will be #world because it's the
     * only one being directly affected by the style.
     *
     * @param identifier a String of identifiers
     *
     * @throws InvalidSyntaxException if illegal certain Illegal characters
     * are encountered or from the methods called (createCSS and getBaseSelector);
     */
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
            throw this.IVSE;
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

            if(styleParts[0].toLowerCase().equals("filter")){
                css.addField(styleParts[0], "No support for " + styleParts[0] + " prop");
            }
            else if(styleParts.length > 2) {
                System.out.println(styleString);
                throw this.IVSE;
            }
            else css.addField(styleParts[0], styleParts[1]);
        }

        return css;
    }

    private String getBlock(StringBuilder cssString, int end) throws InvalidSyntaxException {
        int openCount = 0;

        if(cssString.substring(0, cssString.indexOf("{")).contains("@")){
            int atEnd = cssString.indexOf("}}");

            if(atEnd < 0) throw this.IVSE;

            cssString.delete(0, atEnd + 2);
            return "";
        }

        String block = cssString.substring(0, end + 1);
        cssString.delete(0, end + 1);

        for(int i = 0; i < block.length(); i++){
            if(block.charAt(i) == '{'){
                openCount++;

                if(openCount > 1){
                    System.out.println(block);
                    throw this.IVSE;
                }
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

        if(!(hasStart || hasEnd)){
//            this.inComment
            return line;
        }

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
        long start;
        long end;
        long timeTaken;

        try {
            testParser = new Parser("style2.css", globMap);
            start =  System.currentTimeMillis();
            testParser.parse();
            end = System.currentTimeMillis();
            timeTaken = end - start;

            globMap.forEach((key, val) -> {
                if(val.size() > 1) {
                    System.out.println(key);
                    System.out.println("_________________________________________________________________");
                    val.forEach(System.out::println);
                    System.out.println("=====================================================================");
                }
            });

            System.out.println("\ntook: " + timeTaken + "ms");
            System.out.println("Size of table: " + globMap.size());

        } catch (Exception iee){
            iee.printStackTrace();
        }
    }
}

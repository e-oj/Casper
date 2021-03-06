package CSS_Parser;

import Exceptions.InvalidExtensionException;
import Exceptions.InvalidSyntaxException;
import Utils.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;

/**
 * CSS parser for Casper. Parses the given CSS file and stores
 * it's selectors/identifiers and values in a Map. This map
 * will be passed into the constructor when the object is created
 * and will be used to determine if multiple CSS style objects
 * are linked to the same element, classname or id.
 *
 * @author Emmanuel Olaojo
 * @author Obinna Elobi
 * @since 6/1/16
 */
public class Parser implements Runnable {
    private static final String OPEN_COMMENT = "/*";
    private static final String CLOSE_COMMENT = "*/";
    private String fileName;
    private final Map<String, List<CSS>> globMap;
    private BufferedReader br;
    private boolean inComment = false;
    private InvalidSyntaxException IVSE;
    private boolean isThread = false;
    private final Object LOCK;

    /**
     * Constructor takes a file name and the Map that stores the
     * parsed objects. The extension of the provided file must be css.
     * This is the first validity check carried out on the file. After
     * confirming the extension, we attempt to open the file and initialize
     * the object's properties.
     *
     * @param lock the lock to use
     * @param globMap The Map that stores the parsed objects
     *
     * @throws InvalidExtensionException if the file has an Invalid extension.
     */
    public Parser(String fileName, Map<String, List<CSS>> globMap, Object lock) throws InvalidExtensionException {

        this.fileName = fileName;
        this.globMap = globMap;
        this.LOCK = lock;
        this.IVSE = new InvalidSyntaxException(this.fileName);
    }

    public Parser(Map<String, List<CSS>> globMap) {
        this.LOCK = null;
        this.globMap = globMap;
        this.IVSE = new InvalidSyntaxException(this.fileName);
    }

    public void setFile(String fileName) throws FileNotFoundException, InvalidExtensionException {
        if (!validExt(fileName)) throw new InvalidExtensionException(fileName);

        this.br = new BufferedReader(new FileReader(new File(fileName)));
        this.fileName = fileName;
        this.IVSE = new InvalidSyntaxException(fileName);
    }

    public void setFile() throws FileNotFoundException, InvalidExtensionException {
        if(this.fileName == null) throw new FileNotFoundException("Filename is not set");
        if (!validExt(this.fileName)) throw new InvalidExtensionException(fileName);

        this.br = new BufferedReader(new FileReader(new File(this.fileName)));
        this.IVSE = new InvalidSyntaxException(fileName);
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
     * inComment is true, it checks the current line for the close comment
     * symbol and if a close comment is present, the line gets added to the
     * StringBuilder, else, the line is seen as a comment and is
     * ignored. When all the lines have been added to the StringBuilder,
     * we run it through the parseCSS method for further processing.
     *
     * @throws InvalidSyntaxException from the parseCSS method
     */
    public void parse() throws InvalidSyntaxException{
        StringBuilder minified = new StringBuilder();

        this.br.lines().forEach(line -> {
            if(!this.inComment || line.contains(CLOSE_COMMENT)) {
                line = this.stripComments(line.trim());

                if (line.length() > 0) {
                    minified.append(line.toLowerCase());
                }
            }
        });

        try {
            br.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        parseCSS(minified);
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
            
            if(block.length() > 0){
                if(isThread) parseCssBlockAsync(block);
                else parseCssBlock(block);
            }
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
     * @throws InvalidSyntaxException if certain Illegal characters
     * are encountered or from the methods called (createCSS and getBaseSelector);
     */
    private void parseCssBlock(String cssBlock) throws InvalidSyntaxException {
        String[] styleParts = cssBlock.split("\\{|}");
        String identifiers = styleParts[0];
        String style = styleParts[1];
        CSS css = createCSS(style, identifiers);
        String[] iSplit = identifiers.split(",");
        List<CSS> styleList;

        if(identifiers.contains(";")) throw this.IVSE;

        for(String s: iSplit){
            String key = this.getBaseSelector(s);

            if(globMap.containsKey(key)){
                globMap.get(key).add(css);
            }
            else{
                styleList = new ArrayList<>();
                styleList.add(css);
                globMap.put(key, styleList);
            }
        }
    }

    /**
     * Asynchronously adds a css block to a list in the map.
     * Updating the contents of a list that's already in the map is not externally
     * synchronized because the list has been made thread safe by Collections.synchronizedList.
     * We do have to synchronize externally when adding a new list to the map to prevent
     * a race condition causing a newly created list to be overwritten.
     *
     * This approach has an advantage over synchronizing the whole map which is: any thread
     * can access the table at any time and will only wait if another thread has the list
     * it's trying to access as opposed to all threads waiting for one thread to finish its
     * operation. The only time we block all the threads is when a thread is creating a new list
     * in the map.
     *
     * @param cssBlock a css String
     *
     * @throws InvalidSyntaxException certain Illegal characters
     * are encountered or from the methods called (createCSS and getBaseSelector);
     */
    private void parseCssBlockAsync(String cssBlock) throws InvalidSyntaxException {
        String[] styleParts = cssBlock.split("\\{|}");
        String identifiers = styleParts[0];
        String style = styleParts[1];
        CSS css = createCSS(style, identifiers);
        String[] iSplit = identifiers.split(",");
        List<CSS> styleList;

        if(identifiers.contains(";")) throw this.IVSE;

        for(String s: iSplit){
            String key = this.getBaseSelector(s);

            if(globMap.containsKey(key)) {
                boolean success = addToMap(key, css);

                //fail safe
                while (!success){
                    success = addToMap(key, css);
                }
            }
            else if(LOCK != null){
                synchronized (LOCK) {
                    if(!globMap.containsKey(key)) {
                        styleList = Collections.synchronizedList(new ArrayList<>());
                        globMap.put(key, styleList);
                    }

                    addToMap(key, css);
                }
            }
        }
    }

    /**
     * Adds a CSS value to an existing key in the Map.
     *
     * We are catching a null pointer because another thread might have modified
     * the map (maybe caused a rehash resulting in a temporary null value on an existing key)
     *
     * @param key the necessary key
     * @param val a value to be placed in the list associated with key.
     *
     * @return true if no NullPointerException occurs, false otherwise
     */
    private boolean addToMap(String key, CSS val){
        try{
            globMap.get(key).add(val);
        } catch (NullPointerException n){
            System.out.println("Null pointer occured while adding css to list in map");
            return false;
        }

        return true;
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
     * @throws InvalidSyntaxException if certain Illegal characters are encountered
     */
    private String getBaseSelector(String identifier) throws InvalidSyntaxException {
        identifier = identifier.trim();
        int start = identifier.length() - 1;
        int end = identifier.length();
        final String SELECTORS = ".# >+~";
        char first = identifier.charAt(0);

        if(first == '>' || first == '+' || first == '~'){
            System.out.println(identifier);
            throw this.IVSE;
        }
        
        while(!SELECTORS.contains(identifier.charAt(start) + "") && start > 0){
            start--;
        }

        String baseSelector = identifier.substring(start, end).trim();
        char firstChar = baseSelector.charAt(0);

        if(firstChar == '>' || firstChar == '+'|| firstChar == '~')
            return identifier.substring(start + 1, end);

        return baseSelector;
    }

    /**
     * Creates a CSS object from a style String. A style String would look like
     *
     *      width: 20%;height: 20px;color: blue;
     *
     * look at the style String and #FollowTheCode and it will all
     * become clear. #IBeliveInYouFutureEmmanuel or should I say
     * #PresentEmmanuel cuz it'll be the present when you're reading this
     * in the future. #INeedToSleep
     *
     * @param styleString The styleString to create the object from
     * @return a CSS object;
     * @throws InvalidSyntaxException if I detect nonsense
     */
    private CSS createCSS(String styleString, String identifiers) throws InvalidSyntaxException {
        CSS css = new CSS(this.fileName, identifiers);
        String[] styles = styleString.split(";");

        for(String style: styles){
            String[] styleParts = style.split(":");

            if (styleParts[0].equals("filter")) {
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

    /**
     * Gets the string between 0 and the given end (inclusively)
     * from the StringBuilder then it deletes that block from
     * the StringBuilder
     *
     * @param cssString the string to extract a block from
     * @param end the end index (inclusive)
     *
     * @return a css String or an empty string if it's an @property block
     *
     * @throws InvalidSyntaxException if there's some nonsense going on
     */
    private String getBlock(StringBuilder cssString, int end) throws InvalidSyntaxException {
        int openCount = 0;
        String rule = cssString.substring(0, cssString.indexOf("{"));

        //this beauty cuts out @ blocks
        if (rule.contains("@")) {
            boolean complexAt = rule.contains("media")
                    || rule.contains("keyframes")
                    || rule.contains("document")
                    || rule.contains("supports");

            int atEnd = complexAt ? cssString.indexOf("}}") + 2 : cssString.indexOf("}") + 1;

            if(atEnd < 0) throw this.IVSE;

            cssString.delete(0, atEnd);
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

    /**
     * Strips comments from a String.
     *
     * @param line the String to be stripped
     *
     * @return the clean string.
     */
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

    /**
     * Cuts out the Sting between startIndex and endIndex.
     * startIndex is the index of "/*", end index is the index of
     * the closing comment symbol. Follow the code and all will be
     * made clear.
     *
     * @param line the line to strip
     * @param startIndex "/*"
     * @param endIndex the index of the closing comment symbol.
     *
     * @return a string with the appropriate potion cut out.
     */
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

	/**
	 *  Called When the the Parser.java threads are made and "started"
	 **/
    public void run(){
        try {
            this.isThread = true;
            this.setFile();
            parse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        Map<String, List<CSS>> globMap = new HashMap<>();
        Parser testParser = new Parser(globMap);
        long start;
        long end;
        long timeTaken;

        try {
            testParser.setFile("bootstrap.css");
            start =  System.currentTimeMillis();
            testParser.parse();
            end = System.currentTimeMillis();
            timeTaken = end - start;

            Utilities.logMap(globMap);

            System.out.println("\ntook: " + timeTaken + "ms");
            System.out.println("Size of table: " + globMap.size());

        } catch (Exception iee){
            iee.printStackTrace();
        }
    }
}

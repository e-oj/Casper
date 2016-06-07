package CSS_Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Emmanuel Olaojo
 * @since 6/6/16
 */
public class MultiParser {
    private List<String> files;
    private Parser parser;

    public MultiParser(Map<String, List<CSS>> globMap, List<String> files) {
        this.files = files;
        this.parser = new Parser(globMap);
    }

    //synchronous
    public void parseSync() {
        files.forEach(file -> {
            try {
                parser.setFile(file);
                parser.parse();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });

        System.out.println("Done parsing");
    }

    //multithreaded
    public void parseAsync() {

    }

    public static void main(String[] args) {
        Map<String, List<CSS>> globMap = new HashMap<>();
        List<String> files = new ArrayList<>();
        MultiParser parser = new MultiParser(globMap, files);
        long start;
        long end;
        long timeTaken;

        files.add("style2.css");
        files.add("style2.css");
//        files.add("bootstrap.css");
//        files.add("bootstrap.min.css");

        start = System.currentTimeMillis();
        parser.parseSync();
        end = System.currentTimeMillis();

        timeTaken = end - start;

        globMap.forEach((key, val) -> {
            if (val.size() > 1) {
                System.out.println(key);
                System.out.println("_________________________________________________________________");
                val.forEach(System.out::println);
                System.out.println("=====================================================================");
            }
        });

        System.out.println("\ntook: " + timeTaken + "ms");
        System.out.println("Size of table: " + globMap.size());
    }
}

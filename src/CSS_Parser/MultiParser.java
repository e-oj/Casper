package CSS_Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Emmanuel Olaojo
 * @contributor Obinna Elobi
 * @since 6/6/16
 */
public class MultiParser extends Thread{
    private List<String> files;
    private Parser parser;
    private Map<String, List<CSS>> globMap;

    public MultiParser( List<String> files, Map<String, List<CSS>> globMap) {
        this.files = files;
        this.globMap = globMap;
        
        
    }

    //synchronous
    public void parseSync() {
    	parser = new Parser(globMap);
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
    	files.forEach(file ->  {
    		
    		try {
    			
				new Thread(new Parser(file, globMap)).start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		System.out.println("parsing started");
			
    	});
    	
    	
    }

    public static void main(String[] args) {
    	Map<String, List<CSS>> globMap = new HashMap<>();
        List<String> files = new ArrayList<>();
        MultiParser parser = new MultiParser(files, globMap);
        long start, start2;
        long end, end2;
        long timeTaken, timeTaken2;

        files.add("style2.css");
        files.add("style2.css");
        files.add("bootstrap.css");
        files.add("bootstrap.min.css");

        //Async Call
        start = System.currentTimeMillis();
        parser.parseAsync();
        end = System.currentTimeMillis();

        timeTaken = end - start;
        
        //Sync Call
        start2 = System.currentTimeMillis();
        parser.parseSync();
        end2 = System.currentTimeMillis();

        timeTaken2 = end2 - start2;
        
        
        /**
         * make the multiparser wait for the other threads
         */
        try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        globMap.forEach((key, val) -> {
            if (val.size() > 1) {
                System.out.println(key);
                System.out.println("_________________________________________________________________");
                val.forEach(System.out::println);
                System.out.println("=====================================================================");
            }
        });

        System.out.println("\nAsync took: " + timeTaken + "ms");
        System.out.println("\nSync took: " + timeTaken2 + "ms");
        System.out.println("Size of table: " + globMap.size());
    }
}

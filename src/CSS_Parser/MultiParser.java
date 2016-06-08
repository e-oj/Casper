package CSS_Parser;

import Utils.Logger;
import Utils.Utilities;

import java.util.*;

/**
 * @author Emmanuel Olaojo
 * @author  Obinna Elobi
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
    	});
    }

    public static void main(String[] args) {
    	Map<String, List<CSS>> globMap = new HashMap<>();
    	Map<String, List<CSS>> globMap2 = new HashMap<>();
        List<String> files = new ArrayList<>();
        MultiParser parser = new MultiParser(files, globMap);
        MultiParser parser2 = new MultiParser(files, globMap2);
        Logger asyncLog = new Logger("async.txt", false);
        Logger syncLog = new Logger("sync.txt", false);
        Logger testLog = new Logger("test.txt", true);

        long start, start2;
        long end, end2;
        long timeTaken, timeTaken2;

        files.add("style2.css");
        files.add("style2.css");
        for(int i=0; i<55; i++){
            files.add("bootstrap.css");
        }
        files.add("bootstrap.min.css");

        //Async Call
        start = System.currentTimeMillis();
        parser.parseAsync();

        /**
         * make the multiparser wait for the other threads
         */
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        end = System.currentTimeMillis();

        timeTaken = end - start;

        //Sync Call
        start2 = System.currentTimeMillis();
        parser2.parseSync();
        end2 = System.currentTimeMillis();

        timeTaken2 = end2 - start2;



        System.out.println("done Async");
        Utilities.logMap(globMap, asyncLog);
        Utilities.logMap(globMap2, syncLog);

        testLog.println("\nAsync took: " + timeTaken + "ms");
        testLog.println("\nSync took: " + timeTaken2 + "ms");
        testLog.println("\nSize of table: " + globMap.size());
        testLog.println("______________________________________________________________________________________");

        System.out.println("\nAsync took: " + timeTaken + "ms");
        System.out.println("\nSync took: " + timeTaken2 + "ms");
        System.out.println("\nSize of table: " + globMap.size() + "\n");

        Scanner in = new Scanner(System.in);
        System.out.print("Delete log file (" + testLog.getFileName() +")? y/n: ");
        if(in.nextLine().trim().charAt(0) == 'y') testLog.cleanUp();

        System.out.print("Delete log files (" + asyncLog.getFileName() + ", " + syncLog.getFileName() + ")? y/n: ");

        if(in.nextLine().trim().charAt(0) == 'y'){
            asyncLog.cleanUp();
            syncLog.cleanUp();
        }
    }
}

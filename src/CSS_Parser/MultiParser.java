package CSS_Parser;

import Utils.Logger;
import Utils.Utilities;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Emmanuel Olaojo
 * @author  Obinna Elobi
 * @since 6/6/16
 */
public class MultiParser extends Thread{
    private List<String> files;
    private Parser parser;
    private Map<String, List<CSS>> globMap;
    private List<Future> futures = new LinkedList<>();
    public final int MAX_POOL_SIZE;

    public MultiParser( List<String> files, Map<String, List<CSS>> globMap) {
        int poolSize = 10;
        this.files = files;
        this.globMap = globMap;
        this.MAX_POOL_SIZE = files.size() < poolSize ? files.size() : poolSize;
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

        System.out.println("Done sync");
    }

    //multithreaded
    public void parseAsync() {
        ExecutorService exec = Executors.newFixedThreadPool(this.MAX_POOL_SIZE);

        files.forEach(file ->  {
            try {
                this.futures.add(exec.submit(new Parser(file, globMap)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        exec.shutdown();
    }

    public void endAsync(){
        this.futures.forEach(future -> {
            try {
                //equivalent to thread.join();
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
    	final Map<String, List<CSS>> globMap = new HashMap<>();
    	final Map<String, List<CSS>> globMap2 = new HashMap<>();
        List<String> files = new ArrayList<>();
        int numFiles = 1000;
        String testFile = "bootstrap.css";

        for(int i=0; i<numFiles; i++){
            files.add(testFile);
        }

        MultiParser parser = new MultiParser(files, globMap);
        MultiParser parser2 = new MultiParser(files, globMap2);
        Logger asyncLog = new Logger("async.txt", false);
        Logger syncLog = new Logger("sync.txt", false);
        Logger testLog = new Logger("test.txt", true);

        long start, start2;
        long end, end2;
        long timeTaken, timeTaken2;

        //Async Call
        start = System.currentTimeMillis();
        parser.parseAsync();

        //make the multiParser wait for the other threads
        parser.endAsync();

        System.out.println("done Async");
        end = System.currentTimeMillis();
        timeTaken = end - start;

        //Sync Call
        start2 = System.currentTimeMillis();
        parser2.parseSync();
        end2 = System.currentTimeMillis();

        timeTaken2 = end2 - start2;

        //log the maps to a file
        Utilities.logMap(globMap, asyncLog);
        Utilities.logMap(globMap2, syncLog);

        testLog.println("Using: " + globMap.getClass().getSimpleName());
        testLog.println("\nTesting on " + numFiles + " " + testFile + " files");
        testLog.println("\nWith " + parser.MAX_POOL_SIZE + " threads");
        testLog.println("\nAsync took: " + timeTaken + "ms");
        testLog.println("\nSync took: " + timeTaken2 + "ms");
        testLog.println("\nSize of table: " + globMap.size());
        testLog.println("______________________________________________________________________________________");

        System.out.println("Using: " + globMap.getClass().getSimpleName());
        System.out.println("\nAsync took: " + timeTaken + "ms");
        System.out.println("\nSync took: " + timeTaken2 + "ms");
        System.out.println("\nSize of table: " + globMap.size() + "\n");

        Scanner in = new Scanner(System.in);
        System.out.print("Delete log file (" + testLog.getFileName() +")? y/n: ");
        if(in.nextLine().trim().toLowerCase().charAt(0) == 'y') testLog.cleanUp();

        System.out.print("Delete log files (" + asyncLog.getFileName() + ", " + syncLog.getFileName() + ")? y/n: ");

        if(in.nextLine().trim().toLowerCase().charAt(0) == 'y'){
            asyncLog.cleanUp();
            syncLog.cleanUp();
        }
    }
}

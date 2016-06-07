package Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * @author Emmanuel Olaojo
 * @since 6/7/16
 */
public class Logger {
    PrintWriter pw;
    String fileName;
    File logFile;

    public Logger(String fileName, boolean append){
        try{
            this.logFile = new File(fileName);
            this.pw = new PrintWriter(new BufferedWriter(new FileWriter(logFile, append)));
            this.fileName = fileName;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void println(Object o){
        this.pw.println(o.toString());
        this.pw.flush();
    }

    public void print(Object o){
        this.pw.print(o.toString());
    }

    public String getFileName(){
        return this.fileName;
    }

    public void cleanUp(){
        if(this.logFile.delete()){
            System.out.println("log file deleted");
        } else{
            System.err.println("could not delete log file");
        }
    }
}

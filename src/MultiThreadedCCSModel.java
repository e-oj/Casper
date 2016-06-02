import java.io.File;
import java.util.HashMap;

public class MultiThreadedCCSModel extends CCSModel {

	/*
	 * Idea is one thread per CSS file
	 * each thread is made and passed its own CSS file and a hashmap object which is used by all the threads
	 * to store their selectors and then compared for conflicts.
	 */

	class Crawlers implements Runnable
	{
		//myFile is the current threads CSS file
		// UniversalMap is the hashmap they all put their elements in
		private File myFile;  
		private HashMap UniversalMap;

		public Crawlers(File file, HashMap UniversalMap){
			myFile = file;
			this.UniversalMap = UniversalMap;
		}

		public void run(){
			/*
			 * Do crawler things and what not 
			 */

		}
	}
}

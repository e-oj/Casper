import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class CCSModel {
	
	
    /*
     * Temporary Brute Force pattern matcher(need to make a regex for CSS selectors)
     * text is supposed to be a list of the contents of the CSS file, with the spaces taken out
     * pattern is the pattern we are looking for in a list
     */
	protected boolean findBrute(List<Character> text, List<Character> pattern) 
	{
		int n = text.size();
		int m = pattern.size();
		for (int i = 0; i <= n - m; i++) { // try every starting index 
			// within text
			int k = 0; // k is index into pattern
			while (k < m && text.get(i + k) == pattern.get(k))
				// kth character of pattern matches
				k++;
			if (k == m) // if we reach the end of the pattern,
				return true; // substring text[i..i+m-1] is a match
		}

		return false; // search failed
	}
	

	
	protected void parseStringFile(String myFile){
		String p = "Insert regex here";
		Pattern pattern = Pattern.compile(p);
		Matcher matcher = pattern.matcher(myFile);
		if (matcher.find()){
		System.out.println(matcher.group(1));
		System.out.println(matcher.group(2));
		System.out.println(matcher.group(3));
		System.out.println(matcher.group(4));
		}
	}
	
	/*
	 * Convert the css file to a string, for smaller files.
	 */
	
	protected String fileToString(String path, Charset encoding) 
			  throws IOException 
			{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, encoding);
			}
			
	/*
	 * To parse files block by block (which overlap), better for larger files
	 */

	protected void parseBlockbyBlock(String Path){
        try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(Path)));
		} catch (FileNotFoundException fnf) {
			// TODO Auto-generated catch block
			System.out.println("fnf");
			fnf.printStackTrace();
		}
		
	}

}

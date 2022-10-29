package compareNDA;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class NDAComparer {

	public static void main(String[] args) {
		
		//Name of the files
		String referenceFileName, updateFileName;
		File referenceFile, updateFile;
		//Read from stdin
		BufferedReader br = null; 
		//Analyzer of the file
		SentenceReader differenceCheck = null;

		try {
			//Get reference file name
			System.out.print("Insert reference file path: ");
			br = new BufferedReader(new InputStreamReader(System.in));
			referenceFileName = br.readLine();
			
			//Get update file name
			System.out.print("Insert the path to the file to compare: ");
			br = new BufferedReader(new InputStreamReader(System.in));
			updateFileName = br.readLine();
			
			referenceFile = new File(referenceFileName);
			updateFile = new File(updateFileName);
			differenceCheck = new SentenceReader(referenceFile, updateFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		
		differenceCheck.analyzeFiles();
		
		

	}

}

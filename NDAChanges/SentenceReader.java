package compareNDA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SentenceReader {
	
	///home/stefan/Documents/Tirocinio/NDA_Change
	
	//File to work with
	private File referenceFile, updateFile;
	private BufferedReader referenceReader, updateReader;
	//Row where's found a change
	private int referenceRow = 0;
	//Default separators
	private char separators[] = {' ', '\n', '\t', ','};
	//end sentence
	private String endFileDelimiter = "##END##", formToCompile = "#?";

	//Constructor
	public SentenceReader(File referenceFile, File updateFile) {
		this.referenceFile = referenceFile;
		this.updateFile = updateFile;
	
		//Try open files
		try {
			//Open Reference file
			referenceReader = new BufferedReader(new FileReader(referenceFile));
			//Open Update file
			updateReader = new BufferedReader(new FileReader(updateFile));
		}
		catch(FileNotFoundException e) {
			System.out.println("[Error Opening Files]: The given files aren't correct");
			e.printStackTrace();
		}
	}
	
	//Create the string of a sentence
	private String createSentence(BufferedReader reader) {
		
		//Char by char
		char charRead;
		//Read char
		int r;
		//Found a separator
		boolean end = false;
		//Result string ("" = error)
		String result = "";
		
		try {
			while(end == false && (r = reader.read()) != -1) {
				
				//Conversion int -> char
				charRead = (char) r;
				
				//Check if the char is a separator
				for(int i = 0; i < separators.length; i++) {
					if(charRead == separators[i])
						end = true;
				}
				
				result = result+charRead;
				
				
			}
		}
		catch(IOException e) {
			System.out.println("[Error Reading File]: Unexpected error");
			e.printStackTrace();
		}
		
		
		//If the file has ended
		if(end == false)
			return endFileDelimiter;
		
		return result;		
	}
	
	public int analyzeFiles() {
		
		//The two sentence
		String updateSentence, referenceSentence, spaceless;
		//Understand if we can procede reading de reference file
		boolean match = false, print = false;;
		//Number of mismatch
		int changeNumber = 0;
		
		//Initialize referenceSentence
		referenceSentence = createSentence(referenceReader);
		
		while((updateSentence = createSentence(updateReader)).compareTo(endFileDelimiter) != 0) {
			
			
			//If match keep being correct keep reading from both files
			if(match == true || (referenceSentence.substring(0, referenceSentence.length()-1).compareTo(formToCompile) == 0))
				referenceSentence = createSentence(referenceReader);
			
			//If sentences are different, stop reading from reference until we find correct sentence
			if(updateSentence.compareToIgnoreCase(referenceSentence) != 0) {
				match = false;
				print = true;
				changeNumber++;
				System.out.print(updateSentence + " - " + referenceSentence);
			}
			else {
				match = true;
				if(print == true) {
					print = false;
				}
			}
			
			
		}
		
		try {
			referenceReader.close();
			updateReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return changeNumber;
		
	}
	
	
	private void printChangesToFile(BufferedWriter fileOut) {
		
	}
	
}

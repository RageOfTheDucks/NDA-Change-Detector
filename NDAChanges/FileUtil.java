package pkg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;

public class FileUtil {

	private String fileName;
	private  FileReader fr;
	private FileWriter fw;
	private File file;
	
	//Constructor
	public FileUtil(String fileName) {
		this.fileName = fileName;
	}
	public FileUtil(File file) {
		this.file = file;
	}
	
	
	//Open a file to read
	private File openFileR() {
		File fileIn = new File(fileName);
		try {
			fr = new FileReader(fileIn);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return fileIn;
	}
	//Close the file read
	private void closeFileR() {
		try {
			fr.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Open a file to write
	private File openFileW(String name) {
		File tempOut = new File("formatted" + name);
		try {
			fw = new FileWriter(tempOut);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return tempOut;
	}
	//Close the file written
	private void closeFileW() {
		try {
			fw.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Change the file to analyze and reset fr, fw
	public void setFileName (String name) {
		this.fileName=name;
		try {
			this.fr.reset();
			this.fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	//from the fileName specified trim all excessive spaces 
	public File removeNewLine() {
		
		int charRead;
		char charByChar;
		int find = 0, spaces = 0;
		
		//Open the files that will be read and modify
		File fileIn = openFileR();
		File fileOut = openFileW(fileIn.getName());
		
		
		try {
			//Read char by char untill EOF and trim excessive \n
			while((charRead = fr.read()) != -1) {
				charByChar = (char) charRead;
				if(charByChar != '\n' && charByChar != '\r') {
					if(find >= 2 && charByChar == ' ') {
						
					}
					else {
						if(find > 2)
							fw.write('\n');
						if(charByChar == ' ') {
							spaces++;
							if(spaces == 2) {
								fw.write('\t');
								spaces = 0;
							}
						}
						else {
							if(spaces == 1) {
								spaces = 0;
								fw.write(' ');
							}
							fw.write(charByChar);
						}
						find = 0;
					}
				}
				else {
					find ++;
				}				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Close the worked files
		closeFileW();
		closeFileR();
		
		//The file trimmed
		return fileOut;
	}
	
	//Get differences from changed lines
	public Map<String,String> getDelimiters (LinkedHashMap<String, String[]> changes) {
		//Set the delimiters that will identify the changes
		DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .ignoreWhiteSpaces(false)
                .oldTag(f -> "~")
                .newTag(f -> "#")
                .build();
		
		//ArrayList of strings with all the changes showcased
		ArrayList<String> baseList = new ArrayList<String>();
		ArrayList<String> revList = new ArrayList<String>();
		for (String key : changes.keySet()) 
			if (key.startsWith("Change")) {
				baseList.add(changes.get(key)[0]);
				revList.add(changes.get(key)[1]);
			}
		List<DiffRow> rows = generator.generateDiffRows(baseList, revList);
		
		//Extract all the changes and relative contexts
		ChangeExtractor ce = new ChangeExtractor();
		Map<String, String> lineChanges;
		Map<String, String> lineComparisons = new HashMap<String, String>();
		for (DiffRow row : rows) {
			//All changes in the old line
			ce.setString(row.getOldLine());
			ce.fillMapContext();
			lineChanges = ce.getChanges();
			//Add all the changes
			for (String key : lineChanges.keySet()) 
				lineComparisons.put(key + "b", lineChanges.get(key));
			
			//All the changes in the new line
			ce.setString(row.getNewLine());
			ce.fillMapContext();
			lineChanges = ce.getChanges();
			//Add all the changes
			for (String key : lineChanges.keySet()) 
				lineComparisons.put(key + "r", lineChanges.get(key));

		    System.out.println(row.getOldLine() + System.lineSeparator() + row.getNewLine());
			
		}
		return lineComparisons;
	}
	
	
	public File changesToTxt(File originalFile, File revisedFile) {
		//Build simple lists of the lines of the two text files
		
		try {
			List<String> original = Files.readAllLines(new File(originalFile.getName()).toPath(), StandardCharsets.ISO_8859_1);
			List<String> revised = Files.readAllLines(new File(revisedFile.getName()).toPath(), StandardCharsets.ISO_8859_1);
			//generating diff information.
			Patch<String> diff = DiffUtils.diff(original, revised);

			//generating unified diff format
			List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff("original-file.txt", "modified-file.txt", original, diff, 0);
			
			File outFile = new File("changes.txt");
			
			FileWriter fw = new FileWriter(outFile);
			//write the output on an output file
			for (String s : unifiedDiff) 
				fw.write(s + "\n");
			fw.close();
			
			//deletion of the formatted temporary files
			if (!originalFile.delete()) throw new IOException();
			if (!revisedFile.delete()) throw new IOException();
			
			return outFile;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public HashMap<String,String> readBaseLog() {
		HashMap<String,String> logProperties = new HashMap<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line = br.readLine()) != null) {
				int index = line.indexOf('=');
				String key = line.substring(0, index);
				String value = line.substring(index + 1);
				
				logProperties.put(key, value);
			}
			br.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return logProperties;
	}
	
	public boolean writeBaseLog(HashMap<String,String>logProperties) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for (String property : logProperties.keySet()) {
				bw.write(property + "=" + logProperties.get(property));
			}
			bw.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}

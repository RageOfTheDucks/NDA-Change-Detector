package coso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class NDAComparer {
	private Map<Integer, String[]> fieldsMap = new LinkedHashMap<Integer,String[]>();
	private Map<String, String[]> changesMap = new LinkedHashMap<String, String[]>();
	private int fieldIndex;
	
	//Constructor
	public NDAComparer() {
		this.fieldIndex = 0;
		this.fieldsMap.clear();
		this.changesMap.clear();
	}

	//Get Fields Map
	public Map<Integer, String[]> getFieldsMap() {
		return fieldsMap;
	}

	//Get Changes Map
	public Map<String, String[]> getChangesMap() {
		return changesMap;
	}

	//Extract all the form fields compiled
	private void formfieldsMapHandler(String base, String rev, int line) {
		//System.out.println("BASE:\n"+ base +"\nREV:\n"+ rev);
		
		String baseP1, baseP2;
		String fieldKey = null, fieldValue = null;
		int baseFirst = -1, baseLast = 0, revFirst = 0, revLast = 0;
		boolean repeat = true;
		
		//Split the line in order to obtain, if it is possible the form fieldsMap
		if (base.contains("[name] [jobtitle] [name] [jobtitle]"))
			fieldsMap.put(fieldIndex, new String[] {"Name - Jobtitle, Name - Jobtitle", rev});
		else 
			while (repeat) {
				fieldIndex++;
				//Extraction of the string before the field key
				baseP1 = base.substring(baseFirst + 1, (baseLast = base.indexOf('[', baseFirst)));
				baseFirst = baseLast;
				
				//Extraction of the name of the field key
				fieldKey = base.substring(baseFirst + 1, (baseLast = base.indexOf(']', baseFirst)));
				baseFirst = baseLast;
				
				//Extraction of the string after the field key
				baseLast = base.indexOf('[', baseFirst);
				if (baseLast > -1) 
					baseP2 = base.substring(baseFirst + 1, baseLast); 
				else {
					baseP2 = base.substring(baseFirst + 1);
					repeat = false;
				}
				
				if (rev.indexOf(baseP1, revFirst) == revFirst) {
					revFirst += baseP1.length();
					if (baseP2.equals("")) 
						fieldValue = rev.substring(revFirst);
					else {
						revLast = rev.indexOf(baseP2, revFirst);
						if (revLast == -1) {
							//Change in the text
							changesMap.put("Change@"+ line, new String[] {base, rev});
							break;
						}
						fieldValue = rev.substring(revFirst, revLast);
						
						//field not filled
						if (fieldValue.isBlank())
							changesMap.put("Unfilled@"+ line, new String[] {base, rev});
						
						revFirst = revLast;
					}
					fieldsMap.put(fieldIndex, new String[] {fieldKey, fieldValue});
					
				}
				else {
					//Change in the text
					changesMap.put("Change@"+ line, new String[] {base, rev});
					break;
				}	
			}
	}
	
	//Verify the changes in the two files categorizing them in Addition, Deletion, Changes
	public void computeComparison(File file) {
		try {
			if (file == null) throw new FileNotFoundException(); 
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			int index = 0;
			String line;
			while ((line = br.readLine()) != null) {
				index++;
				if (index > 2) {
					String baseLine = line.substring(line.indexOf('-') + 1, line.indexOf(','));
					int baseEdit = Integer.parseInt(line.substring(line.indexOf(',') + 1, line.indexOf('+')).trim());
					String revLine = line.substring(line.indexOf('+') + 1, line.indexOf(',', line.indexOf('+')));
					int revEdit = Integer.parseInt(line.substring(line.indexOf(',', line.indexOf('+')) + 1, line.indexOf('@', line.indexOf('+'))).trim());
					
					String[][] differences = new String[2][10];
					for (int i = 0; i < baseEdit; i++) {
						//read a new line
						line = br.readLine();
						index++;
						differences[0][i] = line.substring(1);
					}
					for (int i = 0; i < revEdit; i++) {
						//read a new line
						line = br.readLine();
						index++;
						differences[1][i] = line.substring(1);
					}
					
					for (int i = 0; i < Math.max(baseEdit, revEdit); i++) {
						if (differences[0][i] == null) {
							//addition of a new line/sentence
							changesMap.put("Addition@"+ (Integer.parseInt(revLine) + i), new String[] {"#", differences[1][i]});
						}
						else if (differences[1][i] == null) {
							//deletion of a line/sentence
							changesMap.put("Deletion@"+ (Integer.parseInt(baseLine) + i), new String[] {differences[0][i], "#"});
						}
						else if (!differences[0][i].contains("[")) {
							//change in the text
							changesMap.put("Change@"+ (Integer.parseInt(baseLine) + i), new String[] {differences[0][i], differences[1][i]});
						}
						else {
							//fulfilling of a form field
							formfieldsMapHandler(differences[0][i], differences[1][i], (Integer.parseInt(revLine) + i));
						}
					}					
				}
			}
			
			br.close();			
			
		}
		catch (Exception e) {
			e.getStackTrace();
		}
	}
	
	//Print all the form fields and changes found
	public void print() {
		//print of the fieldsMap read
		System.out.println("================================================"+ System.lineSeparator());
		for (int key : fieldsMap.keySet()) {
			String[] value = fieldsMap.get(key);
			System.out.println(key + ". " + value[0] + System.lineSeparator() + "\t" + value[1] + System.lineSeparator());
		}
		
		//change in the text
		for (String key : changesMap.keySet()) {
			String[] value = changesMap.get(key);
			System.out.println("================================================"+ System.lineSeparator());
			System.out.println(key +  System.lineSeparator() + System.lineSeparator() + "BASE FILE:" + System.lineSeparator() + "\t" + value[0] + System.lineSeparator() + "REVISED FILE:" + System.lineSeparator() + "\t" + value[1] + System.lineSeparator());
		}
		
	}
}

package pkg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeExtractor {

	private String chLine;
	//	# -> ADDITION		~ -> REMOVAL
	private char delimiters[] = {'#', '~'};
	private Map<String, String> changes = new HashMap<String, String>();
	
	//Default
	public ChangeExtractor() {
		this.chLine = "EMPTY";
	}
	//With line
	public ChangeExtractor(String line) {
		this.chLine = line;
	}
	
	//Fill methods map
	public int fillMap() {
		//Indexes
		int index = 0, lastIndex = 0, pos = -1;
		boolean status = false;
		String result = "";
		
		//Iterate on the line
		for(int i  = 0; i < delimiters.length; i++) {
			
			pos = 0;
			
			//while i keep find the delimiter
			while((index = chLine.indexOf(delimiters[i], lastIndex+1)) != -1) {
				
				if(status == false) {
					//first delimiter found
					status = true;
				}
				else {
					//Got to the closure delimiter
					status = false;
					//Add string to temporary result
					result += chLine.substring(lastIndex +1 , index);
					//Check if it's done or there are other phrases
					if(chLine.charAt(index + 2) != delimiters[i]){
						if(!result.contains("[")) {
							changes.put(Integer.toString(pos) + delimiters[i], result);
							pos++;
						}
						result = "";
					}
					//Still need to finish the sentence
					else{
						result += " ";
					}
				}
				lastIndex = index;
			}
			
			//No more delimiters[i] found, reset
			index = 0;
			lastIndex = 0;
		}
		
		return pos;
	}

	//Fill methods map with context AAAAAAAAAAAAAAAAAAAAAAAAAAAA
	public int fillMapContext() {
		int index = 0, lastIndex = 0, delNow = -1, pos = 0, end = 0;
		String result = "";
		boolean found, stopPhrase;
		
		for(int i = 0; i < delimiters.length; i++) {
			if(chLine.indexOf(delimiters[i]) != -1) {
				//Not setted yet
				if(delNow == -1) {
					index = chLine.indexOf(delimiters[i]);
					delNow = i;
				}
				//A valule has been setted
				else if(chLine.indexOf(delimiters[i]) < index) {
					index = chLine.indexOf(delimiters[i]);
					delNow = i;
				}
			}
		}
		
		//If i have a delimiter found
		if(delNow != -1) {
			
			
			result = chLine.substring(lastIndex, index);
			changes.put(Integer.toString(pos) + 'X', result);
			pos++;
			result = "";
			lastIndex = index;
			found = true;
			stopPhrase = false;
			
			//At least a delimiter has been found
			while(end != delimiters.length) {
				end = 0;
				while(stopPhrase == false) {
					if(found == true) {
						found = false;
						//Find the other end
						index = chLine.indexOf(delimiters[delNow], lastIndex+1);
						result += chLine.substring(lastIndex+1, index);
						//If the changed part ends
						if(chLine.charAt(index+2) != delimiters[delNow]) {
							stopPhrase = true;
							changes.put(Integer.toString(pos) + delimiters[delNow], result);
							pos++;
						}
						else {
							result += " ";
						}
					}
					
					lastIndex = index;
					
					if(stopPhrase == false && found == false) {
						found = true;
						//Find the start of the next change of the same type
						lastIndex = chLine.indexOf(delimiters[delNow], lastIndex+1);
					}
					
					
					
				}
				result = "";
				stopPhrase = false;
				delNow = -1;
				
				//Find the next delimiter
				for(int i = 0; i < delimiters.length; i++) {
					if(chLine.indexOf(delimiters[i], lastIndex+1) != -1) {
						if(delNow == -1) {
							delNow = i;
							index = chLine.indexOf(delimiters[i], lastIndex+1);
						}
						if(chLine.indexOf(delimiters[i], lastIndex+1) < index) {
							delNow = i;
							index = chLine.indexOf(delimiters[i], lastIndex+1);
						}
					}
					else
						end++;
				}
				
				
				if(end == delimiters.length) {
					result = chLine.substring(lastIndex+1);
					changes.put(Integer.toString(pos) + 'X', result);
				}
				else {
					result = chLine.substring(lastIndex+1, index);
					changes.put(Integer.toString(pos) + 'X', result);
					pos++;
					result = "";
					lastIndex = chLine.indexOf(delimiters[delNow], lastIndex+1);
					found = true;
				}
				
			}
			
		}
		return pos;
	}
	
	//Set a new string to be analyzed
	public void setString(String newLine) {
		this.chLine = newLine;
		changes.clear();
	}
	
	public Map<String,String> getChanges() {
		return changes;
	}
	
	//Print the content for the map
	public void print() {	
		
		List<String> changesByKey = new ArrayList<>(changes.keySet());
		Collections.sort(changesByKey);
		
		for(String key : changesByKey) {
			System.out.println(key + ": " + changes.get(key));
		}
	}
	
}

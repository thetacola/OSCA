package net.oijon.osca;

import java.util.ArrayList;

/**
 * Creates an object that contains a list of strings, and a name that can be used inside
 * strings as placeholders.
 */
public class Category {
	String name;
	ArrayList<String> values ;
	
	private static final String[] WHITESPACE_CHARS = {" ", "\t", "\n", "\r", "\f"};
	public static final Category WHITESPACE = new Category("#", WHITESPACE_CHARS);
	
	/**
	 * Creates a category from an ArrayList of values
	 * @param name The name of the category
	 * @param values Every value in the category
	 */
	public Category(String name, ArrayList<String> values) {
		this.name = name;
		this.values = values;
	}
	
	/**
	 * Creates a category from an array of values
	 * @param name The name of the category
	 * @param values Every value in the category
	 */
	public Category(String name, String[] values) {
		this.name = name;
		this.values = new ArrayList<String>();
		for (int i = 0; i < values.length; i++) {
			this.values.add(values[i]);
		}
	}
	
	/**
	 * Generates all possible strings that could match an input given a
	 * list of categories
	 * @param categories The list of categories that could potentially be in the string
	 * @param input The string to expand
	 * @return Every possible string matching the input
	 */
	public static ArrayList<String> generateMatchesFromCategoryList(ArrayList<Category> categories, String input) {
		boolean allFound = false;
		ArrayList<String> possibleTargets = new ArrayList<String>();
		possibleTargets.add(input);
		while (!allFound) {
			boolean changedAnything = false;
			ArrayList<Integer> removeList = new ArrayList<Integer>();
			for (int i = 0; i < possibleTargets.size(); i++) {
				for (int j = 0; j < categories.size(); j++) {
					int index = possibleTargets.get(i).indexOf(categories.get(j).name);
					if (index != -1) {
						changedAnything = true;
						removeList.add(i);
						ArrayList<String> categoryReplacements = new ArrayList<String>();
						categoryReplacements = categories.get(j).generatePossibleMatches(possibleTargets.get(i));
						for (int k = 0; k < categoryReplacements.size(); k++) {
							possibleTargets.add(categoryReplacements.get(k));
						}
					}
				}
			}
			
			for (int i = 0; i < removeList.size(); i++) {
				possibleTargets.remove(removeList.get(i).intValue());
			}
			
			if (!changedAnything) {
				allFound = true;
			}
		}
		
		return possibleTargets;
	}
	
	/**
	 * Generates possible matches from a string using the current category
	 * @param inputList The inputs to be processed
	 * @return Expanded forms for the given category
	 */
	public ArrayList<String> generatePossibleMatches(ArrayList<String> inputList) {
		ArrayList<String> output = new ArrayList<String>();
		
		for (int i = 0; i < inputList.size(); i++) {
			ArrayList<String> intermediate = generatePossibleMatches(inputList.get(i));
			for (int j = 0; j < intermediate.size(); j++) {
				output.add(intermediate.get(j));
			}
		}
		
		return output;
	}
	
	/**
	 * Replaces the first instance of a category with every possible value
	 * @param input The string to be used to replace
	 * @return The input with the first instance of the category replaced by every possible value
	 */
	public ArrayList<String> replaceFirstInstance(String input) {
		ArrayList<String> output = new ArrayList<String>();
	
		for (int i = 0; i < values.size(); i++) {
			String newString = input.replaceFirst(name, values.get(i));
			output.add(newString);
		}
		
		return output;
	}
	
	/**
	 * Replace a given instance of a category with a given category index
	 * @param input The input to use
	 * @param instance The instance of the category in the input to replace
	 * @param indexInCat The index inside the category of the value the category should be replaced with
	 * @return An expanded version of the input using the given instance and index
	 */
	public String replaceInstance(String input, int instance, int indexInCat) {
		String output = "";
		
		int numFound = 0;
	
		for (int i = 0; i < input.length(); i++) {
			String prefix = input.substring(0, i);
			String substring = input.substring(i);
			
			if (substring.startsWith(name)) {
				numFound++;
			}
			
			if (numFound == instance) {
				String newSubstring = substring.replaceFirst(substring, values.get(indexInCat));
				output = prefix + newSubstring;
				
				break;
			}
		}
		
		return output;
	}
	
	public ArrayList<String> replaceInstance(String input, int instance) {
		ArrayList<String> output = new ArrayList<String>();
		
		int numFound = 0;
	
		for (int i = 0; i < input.length(); i++) {
			String prefix = input.substring(0, i);
			String substring = input.substring(i);
			
			if (substring.startsWith(name)) {
				numFound++;
			}
			
			if (numFound == instance) {
				for (int j = 0; j < values.size(); j++) {
					String newSubstring = substring.replaceFirst(substring, values.get(j));
					String newString = prefix + newSubstring;
					output.add(newString);
				}
				break;
			}
		}
		
		return output;
	}
	
	public ArrayList<String> generatePossibleMatches(String input) {
		ArrayList<String> output = new ArrayList<String>();
		if (input.contains(name)) {
			int numTimes = 0;
			int lastFoundIndex = 0;
			boolean foundAll = false;
			while (!foundAll) {
				int index = input.indexOf(name, lastFoundIndex);
				if (index == -1) {
					foundAll = true;
				} else {
					numTimes++;
					lastFoundIndex = index + 1;
				}
			}
			
			// this is an attempt to put what would usually be the output of many for loops into one
			// its quite computationally expensive
			// there should be values.size() ^ numTimes possible matches
			
			/**
			 * The idea is to treat indexArr as a little-endian n-base number,
			 * with each digit corresponding to an index of the category.
			 * 
			 * The for loop will tick up the array index for a digit.
			 * If (digit + 1) % values.size() == 0, it overflows to the next digit,
			 * setting that one to zero
			 */
			int[] indexArr = new int[numTimes];
			// set the index array
			for (int i = 0; i < indexArr.length; i++) {
				indexArr[i] = 0;
			}
			
			for (int i = 0; i < Math.pow(values.size(), numTimes); i++) {
				String possibility = input;
				for (int j = 0; j < numTimes; j++) {
					possibility = possibility.replaceFirst(name, values.get(indexArr[j]));
				}
				output.add(possibility);
				// after generation, update array
				indexArr[0] = indexArr[0] + 1; // add first digit
				for (int j = 0; j < numTimes - 1; j++) {
					// numTimes - 1 to prevent array overflow, shouldn't be possible but just in case
					if (indexArr[j] % values.size() == 0) {
						indexArr[j] = 0;
						indexArr[j + 1] = indexArr[j + 1] + 1;
					}
				}
			}
			
		} else {
			output.add(input);
		}
		
		return output;
	}

}

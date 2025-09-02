package net.oijon.osca;

import java.util.ArrayList;
import java.util.regex.Pattern;

import net.oijon.olog.Log;
import net.oijon.osca.exception.InvalidRuleSyntaxException;

public class Rule {
	
	static Log log = new Log(System.getProperty("user.home") + "/.osca");

	protected static char[] whitespaces = {' ', '\t', '\n', '\r', '\f'};
	
	String target = "";
	String replacement = "";
	String environment = "";
	String exception = "";
	ArrayList<Category> categories = new ArrayList<Category>();
	ArrayList<String[]> mappings = new ArrayList<String[]>();
	ArrayList<String> exceptions = new ArrayList<String>();
	
	/**
	 * Creates a rule based on an input string
	 * @param ruleStr The string to make the rule from
	 * @throws InvalidRuleSyntaxException Thrown when ruleStr does not follow the expected X/Y/Z/A format
	 */
	public Rule(String ruleStr) throws InvalidRuleSyntaxException {
		parseInputString(ruleStr);
		parseNonceCategories();
		generateMappings();
		parseOptionals();
	}
	/**
	 * Creates a rule based on an input string and a list of categories
	 * @param ruleStr The string to make the rule from
	 * @param categories The list of categories to use
	 * @throws InvalidRuleSyntaxException Thrown when ruleStr does not follow the expected X/Y/Z/A format
	 */
	public Rule(String ruleStr, ArrayList<Category> categories) throws InvalidRuleSyntaxException {
		parseInputString(ruleStr);
		this.categories = categories;
		parseNonceCategories();
		generateMappings();
		parseOptionals();
	}
	
	/**
	 * Creates a rule with a given target, replacement, and environment, along with a category list
	 * @param target The target for the rule
	 * @param replacement The replacement for the rule
	 * @param environment The environment for the rule
	 * @param categories The list of categories to use
	 */
	public Rule(String target, String replacement, String environment, ArrayList<Category> categories) {
		this.target = target;
		this.replacement = replacement;
		this.environment = environment;
		this.categories = categories;
		parseNonceCategories();
		generateMappings();
		parseOptionals();
	}
	
	/**
	 * Creates a rule with a given target, replacement, and environment
	 * @param target The target for the rule
	 * @param replacement The replacement for the rule
	 * @param environment The environment for the rule
	 */
	public Rule(String target, String replacement, String environment) {
		this.target = target;
		this.replacement = replacement;
		this.environment = environment;
		parseNonceCategories();
		generateMappings();
		parseOptionals();
	}
	
	/**
	 * Creates a rule with a given target, replacement, environment, and exception
	 * @param target The target for the rule
	 * @param replacement The replacement for the rule
	 * @param environment The environment for the rule
	 * @param exception The exception for the rule; when the rule should not apply
	 */
	public Rule(String target, String replacement, String environment, String exception) {
		this.target = target;
		this.replacement = replacement;
		this.environment = environment;
		this.exception = exception;
		parseNonceCategories();
		generateMappings();
		parseOptionals();
	}
	/**
	 * Creates a rule with a given target, replacement, environment, and exception, along with a list of categories
	 * @param target The target for the rule
	 * @param replacement The replacement for the rule
	 * @param environment The environment for the rule
	 * @param exception The exception for the rule; when the rule should not apply
	 * @param categories The list of categories to use
	 */
	public Rule(String target, String replacement, String environment, String exception, ArrayList<Category> categories) {
		this.target = target;
		this.replacement = replacement;
		this.environment = environment;
		this.exception = exception;
		this.categories = categories;
		parseNonceCategories();
		generateMappings();
		parseOptionals();
	}
	
	/**
	 * Takes a string from a constructor, and parses it into a rule
	 * @param ruleStr The string to parse
	 * @throws InvalidRuleSyntaxException Thrown when there is a syntax error in the rule
	 */
	private void parseInputString(String ruleStr) throws InvalidRuleSyntaxException {
		String[] split = ruleStr.split("/");
		if (split.length < 3) {
			throw new InvalidRuleSyntaxException("Expected 3-4 parameters, got " + split.length);
		} else {			
			target = split[0];
			replacement = split[1];
			environment = split[2];
			if (split.length >= 4) {
				exception = split[3];
			}
		}
	}
	
	/**
	 * Counts the categories in a given string, using the rule's categories.
	 * Note that this does not count nonce categories.
	 * @param input The string to count the categories in
	 * @return The amount of categories found in the input
	 */
	private int countCategories(String input) {
		int count = 0;
		
		for (int i = 0; i < categories.size(); i++) {
			boolean foundAll = false;
			int lastIndex = 0;
			while (!foundAll) {
				int index = input.indexOf(categories.get(i).name, lastIndex);
				if (index == -1) {
					foundAll = true;
				} else {
					count++;
					lastIndex = index + 1;
				}
			}
		}
		
		return count;
	}
	
	/**
	 * Gets a list of categories found in a string
	 * @param input The string to find categories in
	 * @return A list of each category in the given string
	 */
	private Category[] getCategoriesFromString(String input) {
		int numCats = countCategories(input);
		Category[] categoryIndicies = new Category[numCats];
		categories.sort((a, b) -> { return -1 * compareStringsBySize(a.name, b.name); } );
		
		int numFound = 0;
		for (int i = 0; i < input.length(); i++) {
			String substring = input.substring(i);
			for (int j = 0; j < categories.size(); j++) {
				if (substring.startsWith(categories.get(j).name)) {
					categoryIndicies[numFound] = categories.get(j);
					numFound++;
				}
			}
		}
		
		return categoryIndicies;
	}
	
	/**
	 * Parses optional elements of rules. This is done by creating a new rule for
	 * each possible combination of optional elements.
	 */
	private void parseOptionals() {
		ArrayList<String> targets = parseOptionalsFromString(target);
		ArrayList<String> replacements = parseOptionalsFromString(replacement);
		ArrayList<String> environments = parseOptionalsFromString(environment);
		ArrayList<String> exceptions = parseOptionalsFromString(exception);
		
		// TODO: O(n⁴), eek!
		for (int i = 0; i < targets.size(); i++) {
			for (int j = 0; j < replacements.size(); j++) {
				for (int k = 0; k < environments.size(); k++) {
					for (int l = 0; l < exceptions.size(); l++) {
						if (!(targets.get(i).equals(target)
								& replacements.get(j).equals(replacement)
								& environments.get(k).equals(environment)
								& exceptions.get(l).equals(exception))) {
							Rule r = new Rule(targets.get(i),
									replacements.get(j),
									environments.get(k),
									exceptions.get(l),
									categories);
							for (int m = 0; m < r.mappings.size(); m++) {
								mappings.add(r.mappings.get(m));
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Parses optional categories from a given string
	 * @param input The string to find optional categories in
	 * @return The list of possible strings with the optional categories
	 */
	private ArrayList<String> parseOptionalsFromString(String input) {
		ArrayList<String> output = new ArrayList<String>();
		output.add(input);
		
		boolean done = false;
		
		while (!done) {
			boolean anyChanges = false;
			for (int i = 0; i < output.size(); i++) {
				String oldString = output.get(i);
				int beginIndex = oldString.indexOf('(');
				if (beginIndex != -1) {
					int endIndex = oldString.indexOf(')', beginIndex - 1);
					if (endIndex != -1) {
						anyChanges = true;
						String parenthesizedString = oldString.substring(beginIndex, endIndex + 1);
						String newString = oldString.substring(0, beginIndex)
								+ parenthesizedString.substring(1, parenthesizedString.length() - 1)
								+ oldString.substring(endIndex + 1, oldString.length());
						oldString = oldString.replaceFirst(Pattern.quote(parenthesizedString), "");
						output.set(i, oldString);
						output.add(newString);
						
					}
				}
			}
			if (!anyChanges) {
				done = true;
			}
		}
		
		return output;
	}
	
	/**
	 * Generates the mappings between targets and replacements, attaches them to
	 * their given environments, and handles gemination and metathesis.
	 */
	private void generateMappings() {
		mappings.clear();
		categories.add(Category.WHITESPACE);
		// if a category is given in the target and replacement, the only mapping
		// should be for the given index
		
		/**
		 * ex. let's say we have these cats:
		 * S=ptkq
		 * Z=bdg
		 * V=aeiou
		 * 
		 * for this rule
		 * S/Z/V_V
		 * the following input-output pairs should be processed
		 * ipi → ibi
		 * ate → ade
		 * ukuk → uguk
		 * qoqu → qobu
		 * 
		 * for this rule
		 * Z/S/V_V
		 * the following input-output pairs should be processed
		 * ibi → ipi
		 * ade → ate
		 * uguk → ukuk
		 * qobu → qopu
		 * 
		 * something like this rule should have the following input-output pairs
		 * ZZ/SV/_
		 * ibd → ipe
		 * ibb → ipa
		 * idb → ita
		 * 
		 * however, something like Z/SV/_ should look like this:
		 * ibd → ipate
		 * ibb → ipapa
		 * idb → itepa
		 */
		ArrayList<String[]> targetReplacementPairs = new ArrayList<String[]>();
		ArrayList<String> allEnvs = new ArrayList<String>();
		ArrayList<String> allExps = new ArrayList<String>();
		
		if (replacement.equals("\\\\\\\\")) {
			ArrayList<String> possibleTargets = Category.generateMatchesFromCategoryList(categories, target);
			for (int i = 0; i < possibleTargets.size(); i++) {
				String reverse = possibleTargets.get(i);
				StringBuilder reverser = new StringBuilder();
				reverser.append(reverse);
				reverser.reverse();
				String[] newPair = {possibleTargets.get(i), reverser.toString()};
				targetReplacementPairs.add(newPair);
				// System.out.println("[Debug] - " + Arrays.toString(newPair));
			}
		} else if (countCategories(target) == 0) {
			// one-to-one
			String[] newPair = {target, replacement};
			targetReplacementPairs.add(newPair);
		} else if (countCategories(replacement) == 0) {
			// many-to-one
			ArrayList<String> possibleTargets = Category.generateMatchesFromCategoryList(categories, target);			
			for (int i = 0; i < possibleTargets.size(); i++) {
				String[] newPair = {possibleTargets.get(i), replacement};
				targetReplacementPairs.add(newPair);
			}
		} else {
			// many-to-many
			
			// add bare pair, this'll be removed as it iterates
			String[] barePair = {target, replacement};
			
			targetReplacementPairs.add(barePair);
			
			Category[] targetCategories = getCategoriesFromString(target);
			Category[] replacementCategories = getCategoriesFromString(replacement);
			
			int loopNum;
			if (targetCategories.length < replacementCategories.length) {
				loopNum = targetCategories.length;
			} else {
				loopNum = replacementCategories.length;
			}
			
			for (int i = 0; i < loopNum; i++) {
				Category targetCat = targetCategories[i];
				Category replacementCat = replacementCategories[i];
				
				ArrayList<String[]> newMappings = new ArrayList<String[]>();
				ArrayList<String[]> removalList = new ArrayList<String[]>();
				for (int j = 0; j < targetReplacementPairs.size(); j++) {
					boolean changedAnything = false;
					for (int k = 0; k < targetCat.values.size(); k++) {
						int replacementIndex = k % replacementCat.values.size();
						
						String oldTarget = targetReplacementPairs.get(i)[0];
						String oldReplacement = targetReplacementPairs.get(i)[1];
						
						String newTarget = oldTarget.replaceFirst(targetCat.name, targetCat.values.get(k));
						String newReplacement = oldReplacement.replaceFirst(replacementCat.name, replacementCat.values.get(replacementIndex));
						
						if (!newTarget.equals(oldTarget) | !newReplacement.equals(oldReplacement)) {
							changedAnything = true;
						}
						
						String[] newPair = {newTarget, newReplacement};
						//System.out.println("[Debug] - " + Arrays.toString(newPair));
						newMappings.add(newPair);
						
					}
					if (changedAnything) {
						removalList.add(targetReplacementPairs.get(j));
					}
				}
				
				/**
				 * breaks sometimes, should be fine to keep though
				for (int j = 0; j < removalList.size(); j++) {
					targetReplacementPairs.remove(removalList.get(j));
				}
				**/
				
				for (int j = 0; j < newMappings.size(); j++) {
					targetReplacementPairs.add(newMappings.get(j));
				}
				
			}
			
		}
		
		if (environment.equals("²")) {
			environment = "_²";
		}
		
		if (countCategories(environment) == 0) {
			allEnvs.add(environment);
		} else {
			allEnvs = Category.generateMatchesFromCategoryList(categories, environment);
		}
		
		if (countCategories(exception) == 0) {
			allExps.add(exception);
		} else {
			allExps = Category.generateMatchesFromCategoryList(categories, exception);
		}
		
		for (int i = 0; i < allExps.size(); i++) {
			for (int j = 0; j < targetReplacementPairs.size(); j++) {
				String newExp = allExps.get(i).replace("_", targetReplacementPairs.get(j)[0]);
				exceptions.add(newExp);
			}
		}
		
		for (int i = 0; i < targetReplacementPairs.size(); i++) {
			//System.out.println("[Debug] [Mapping] " + Arrays.toString(targetReplacementPairs.get(i)));
			for (int j = 0; j < allEnvs.size(); j++) {
				String targetWithEnv = allEnvs.get(j).replace("_", targetReplacementPairs.get(i)[0]);
				targetWithEnv = targetWithEnv.replace("²", targetReplacementPairs.get(i)[0]);
				String replacementWithEnv = allEnvs.get(j).replace("_", targetReplacementPairs.get(i)[1]);
				replacementWithEnv = replacementWithEnv.replace("²", targetReplacementPairs.get(i)[0]);
				
				String[] newMapping = {targetWithEnv, replacementWithEnv};
				mappings.add(newMapping);
				//System.out.println("[Debug] [Mapping] - " + Arrays.toString(newMapping));
			}
		}
	}
	
	/**
	 * Sorts two strings by length
	 * @param a The first string to compare
	 * @param b The second string to compare
	 * @return -1 if a<b, 0 if a==b, 1 if a>b
	 */
	private int compareStringsBySize(String a, String b) {
		if (a.length() < b.length()) {
			return -1;
		} else if (a.length() == b.length()) {
			return 0;
		} else {
			return 1;
		}
	}
	
	/**
	 * Parses nonce categories from each field.
	 */
	private void parseNonceCategories() {
		target = parseNonceFromString(target);
		replacement = parseNonceFromString(replacement);
		environment = parseNonceFromString(environment);
		exception = parseNonceFromString(exception);
	}
	
	/**
	 * Finds nonce categories in a given string, and replaces them with the
	 * typical category syntax using a temp name
	 * @param input The string to use
	 * @return The new string, made to handle typical categories
	 */
	private String parseNonceFromString(String input) {
		String output = input;
		
		boolean done = false;
		
		while (!done) {
			int beginIndex = output.indexOf('[');
			if (beginIndex != -1) {
				int endIndex = output.indexOf(']', beginIndex - 1);
				if (endIndex != -1) {
					String newCategoryString = output.substring(beginIndex, endIndex + 1);
					boolean named = false;
					String categoryName = "";
					int endInt = categories.size();
					while (!named) {
						categoryName = "NONCECATEGORY" + endInt;
						named = true;
						for (int i = 0; i < categories.size(); i++) {
							if (categories.get(i).name.equals(categoryName)) {
								endInt++;
								named = false;
								break;
							}
						}
					}
					
					String[] catData = new String[newCategoryString.length() - 2];
					for (int i = 1; i < newCategoryString.length() - 1; i++) {
						catData[i - 1] = Character.toString(newCategoryString.charAt(i));
					}
					
					Category newCategory = new Category(categoryName, catData);
					categories.add(newCategory);
					categories.sort((a, b) -> { return -1 * compareStringsBySize(a.name, b.name); } );
					// System.out.println("[Debug] - Added " + categoryName + " with data " + Arrays.toString(catData));
					output = output.replace(newCategoryString, categoryName);
					// System.out.println("[Debug] - Output is now " + output);
				} else {
					done = true;
				}
			} else {
				done = true;
			}
		}
		
		return output;
	}
	
	/**
	 * Takes a string, runs it through the rule, and gives an output.
	 * @param input The string to run through the rule
	 * @return The output of the rule on the given string
	 */
	public String parse(String input) {
		// add whitespace to mark boundaries
		
		String output = " " + input + " ";
		String suffix = "";
		int firstGlossChar = output.indexOf('‣');
		if (firstGlossChar != -1) {
			suffix = output.substring(firstGlossChar);
			output = output.substring(0, firstGlossChar - 1) + " ";
		}
		
		ArrayList<String[]> newValues = new ArrayList<String[]>();
		ArrayList<String[]> expValues = new ArrayList<String[]>();
		
		int numExpFound = 0;
		for (int i = 0; i < exceptions.size(); i++) {
			boolean foundAll = false;
			if (exceptions.get(i).equals("")) {
				foundAll = true;
			}
			
			int lastIndex = 0;
			while (!foundAll) {
				int index = output.indexOf(exceptions.get(i), lastIndex);
				if (index > -1) {
					String expPlaceholder = "‣‣E" + numExpFound + "‣‣";
					numExpFound++;
					lastIndex = index + expPlaceholder.length();
					output = output.replaceFirst(exceptions.get(i), expPlaceholder);
					String[] pair = {expPlaceholder, exceptions.get(i)};
					expValues.add(pair);
				} else {
					foundAll = true;
				}
			}
		}
		
		for (int i = 0; i < mappings.size(); i++) {
			//System.out.println("[Debug] [Mapping] " + Arrays.toString(mappings.get(i)));
			boolean foundAll = false;
			while (!foundAll) {				
				String placeholder = "‣" + newValues.size() + "‣";
				String ourTarget = mappings.get(i)[0];
				String ourReplacement = mappings.get(i)[1];
				
				//System.out.print("Target " + ourTarget);
				
				while (ourTarget.charAt(0) == '…') {
					ourTarget = ourTarget.substring(1);
				}
				
				while (ourTarget.charAt(ourTarget.length() - 1) == '…') {
					ourTarget = ourTarget.substring(0, ourTarget.length() - 1);
				}
				
				//System.out.println(" becomes " + ourTarget);
				
				if (ourTarget.contains("…")) {
					// a wildcard at the beginning or end doesn't really *do* anything,
					// so they can be ignored
					String[] splitTarget = ourTarget.split("…");
					if (splitTarget.length > 1) {
						for (int j = 0; j < splitTarget.length - 1; j++) {
							int startIndex = output.indexOf(splitTarget[j]);
							int endIndex = output.indexOf(splitTarget[j + 1]);
							//System.out.println("[Debug] - " + output + " " + startIndex + "-" + endIndex);
							if (startIndex > 0 & endIndex > 0 & startIndex < endIndex) {
								startIndex +=  + splitTarget[j].length();
								String inBetween = output.substring(startIndex, endIndex);
								//System.out.println("[Debug] - " + "[" + startIndex + "-" + 
								//endIndex + "] "+ inBetween);
								ourTarget = ourTarget.replaceFirst((splitTarget[j] + "…" + splitTarget[j + 1]),
										(splitTarget[j] + inBetween + splitTarget[j + 1]));
								//System.out.println("[Debug] - " + splitTarget[j] + "…" + splitTarget[j + 1] + " → " + splitTarget[j] + inBetween + splitTarget[j + 1]);
								ourReplacement = ourReplacement.replaceFirst("…", inBetween);
							}
						}
					} else {
						ourTarget = splitTarget[0];
					}
				}
				
				//System.out.println("Searching for '" + ourTarget + "' in '" + output + "'");
				int index = output.indexOf(ourTarget);
				
				if ((index == -1)) {
					foundAll = true;
				} else {
					//System.out.println("[Debug] [Target/Replacement] - " + ourTarget + " → " + ourReplacement);
					output = output.replace(ourTarget, placeholder);
					String[] pair = {placeholder, ourReplacement};
					newValues.add(pair);
				}
			}
		}
		
		for (int i = 0; i < newValues.size(); i++) {
			//System.out.println("[Debug] [Output] - " + newValues.get(i)[0] + "→" + newValues.get(i)[1]);
			output = output.replace(newValues.get(i)[0], newValues.get(i)[1]);
		}
		
		for (int i = 0; i < expValues.size(); i++) {
			output = output.replace(expValues.get(i)[0], expValues.get(i)[1]);
		}
		
		output = output + suffix;
		
		// remove added whitespace
		output = output.substring(1);
		output = output.substring(0, output.length() - 1);
		
		return output;
	}
	
	/**
	 * Parses a list of rules on one string
	 * @param list The list of rules to use
	 * @param input The input to be processed
	 * @return The output of every rule on the input
	 */
	public static String parseList(ArrayList<Rule> list, String input) {
		String output = input;
		
		for (int i = 0; i < list.size(); i++) {
			output = list.get(i).parse(output);
		}
		
		return output;
	}
	
	/**
	 * Parses a list of rules, and makes it an ArrayList
	 * @param ruleList A list of rules, separated by newlines
	 * @return an ArrayList of each rule specified
	 */
	public static ArrayList<Rule> parseFromList(String ruleList) {
		ArrayList<Rule> rules = new ArrayList<Rule>();
		
		String[] split = ruleList.split("\n");
		for (int i = 0; i < split.length; i++) {
			try {
				Rule r = new Rule(split[i]);
				rules.add(r);
			} catch (InvalidRuleSyntaxException e) {
				log.warn("On line №" + i + " (" + split[i] + "): " + e.toString());
				e.printStackTrace();
			}
		}
		
		return rules;
	}
	
	@Override
	public String toString() {
		String str = target + "/" + replacement + "/" + environment;
		if (!exception.equals("")) {
			str += "/" + exception;
		}
		return str;
	}
	
}

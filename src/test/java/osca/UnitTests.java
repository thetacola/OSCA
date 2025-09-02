package osca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import net.oijon.osca.Category;
import net.oijon.osca.Rule;
import net.oijon.osca.exception.InvalidRuleSyntaxException;

public class UnitTests {

	// catch broken rules
	@Test
	@SuppressWarnings("unused")
	void ruleSyntax() {		
		int numCaught = 0;
		
		try {
			Rule rule = new Rule("");
		} catch (InvalidRuleSyntaxException e) {
			numCaught++;
		}
		
		try {
			Rule rule = new Rule("pb");
		} catch (InvalidRuleSyntaxException e) {
			numCaught++;
		}
		
		try {
			Rule rule = new Rule("p/b");
		} catch (InvalidRuleSyntaxException e) {
			numCaught++;
		}
		
		if (numCaught != 3) {
			fail();
		}
	}
	
	// simple replace
	@Test
	void simpleReplace() {
		String ruleStr = "p/b/_";
		String input = "papaba";
		try {
			Rule rule = new Rule(ruleStr);
			String output = rule.parse(input);
			assertEquals("bababa", output);
			
			input = "ppppppp";
			output = rule.parse(input);
			assertEquals("bbbbbbb", output);
			
			input = "this is a test string without any voiceless bilabials";
			output = rule.parse(input);
			assertEquals("this is a test string without any voiceless bilabials", output);
		
			input = "pa$\\.*+?(a)";
			output = rule.parse(input);
			assertEquals("ba$\\.*+?(a)", output);
		} catch (InvalidRuleSyntaxException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// category possibility generation
	@Test
	void categoryPossibilityGeneration() {
		String[] catVals = {"a", "b", "c"};
		Category testCat = new Category("T", catVals);
		System.out.println("T == " + Arrays.toString(catVals));
		
		ArrayList<String> output = testCat.generatePossibleMatches("T");
		System.out.println("T → " + output.toString());
		String[] expectedOutput = catVals;
		assertEquals(3, output.size());
		for (int i = 0; i < expectedOutput.length; i++) {
			assertTrue(output.contains(expectedOutput[i]));
		}
		
		output = testCat.generatePossibleMatches("TT");
		System.out.println("TT → " + output.toString());
		String[] expectedOutput2 = {
					"aa", "ab", "ac",
					"ba", "bb", "bc",
					"ca", "cb", "cc"
				};
		assertEquals(9, output.size());
		for (int i = 0; i < expectedOutput2.length; i++) {
			assertTrue(output.contains(expectedOutput2[i]));
		}
		
		output = testCat.generatePossibleMatches("aTaTa");
		System.out.println("aTaTa → " + output.toString());
		String[] expectedOutput3 = {
					"aaaaa", "aaaba", "aaaca",
					"abaaa", "ababa", "abaca",
					"acaaa", "acaba", "acaca"
				};
		assertEquals(9, output.size());
		for (int i = 0; i < expectedOutput3.length; i++) {
			assertTrue(output.contains(expectedOutput3[i]));
		}
		
		output = testCat.generatePossibleMatches("a");
		System.out.println("a → " + output.toString());
		String[] expectedOutput4 = {"a"};
		assertEquals(1, output.size());
		for (int i = 0; i < expectedOutput4.length; i++) {
			assertTrue(output.contains(expectedOutput4[i]));
		}
	}
	
	// replace on word boundary
	@Test
	void replaceWithBoundary() {
		String ruleStr = "p/b/#_";
		String input = "papaba";
		try {
			Rule rule = new Rule(ruleStr);
			String output = rule.parse(input);
			assertEquals("bapaba", output);
			
			input = "ppp pppp";
			output = rule.parse(input);
			assertEquals("bpp bppp", output);
			
			input = "tab pp\tpp";
			output = rule.parse(input);
			assertEquals("tab bp\tbp", output);
		} catch (InvalidRuleSyntaxException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// replace with category
	@Test
	void replaceWithCategory() {
		String[] vowelVals = {"a", "e", "i", "o", "u", "ə"};
		Category vowels = new Category("V", vowelVals);
		Category vowels2 = new Category("Vowels", vowelVals);
		ArrayList<Category> cats = new ArrayList<Category>();
		cats.add(vowels);
		cats.add(vowels2);
		
		try {
			String ruleStr = "V/a/_#";
			Rule rule = new Rule(ruleStr, cats);
			String output = rule.parse("bə bib bi");
			assertEquals("ba bib ba", output);
			
			ruleStr = "V/e/Vowels_";
			rule = new Rule(ruleStr, cats);
			output = rule.parse("bia biu bai bei");
			assertEquals("bie bie bae bee", output);
			
			ruleStr = "VVowels/e/_";
			rule = new Rule(ruleStr, cats);
			output = rule.parse("bia biu bai bei");
			assertEquals("be be be be", output);
		} catch (InvalidRuleSyntaxException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// replace in environment
	@Test
	void replaceWithEnvironment() {
		String[] vowelVals = {"a", "e", "i", "o", "u"};
		Category vowels = new Category("V", vowelVals);
		ArrayList<Category> cats = new ArrayList<Category>();
		cats.add(vowels);
		
		try {
			String ruleStr = "V/u/_e";
			Rule rule = new Rule(ruleStr, cats);
			String output = rule.parse("boe bieb bioe");
			assertEquals("bue bueb biue", output);
			
			ruleStr = "a/e/b_V";
			rule = new Rule(ruleStr, cats);
			output = rule.parse("bai ba pai pa");
			assertEquals("bei ba pai pa", output);
		} catch (InvalidRuleSyntaxException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	
	// category-to-category mapping
	@Test
	void categoryToCategoryMapping() {
		String[] voicelessVals = {"p", "t", "k"};
		String[] voicedVals = {"b", "d", "g"};
		String[] vowelVals = {"a", "e", "i", "o", "u"};
		Category voiceless = new Category("S", voicelessVals);
		Category voiced = new Category("Z", voicedVals);
		Category vowels = new Category("V", vowelVals);
		ArrayList<Category> cats = new ArrayList<Category>();
		cats.add(voiceless);
		cats.add(voiced);
		cats.add(vowels);
		
		try {
			String ruleStr = "S/Z/V_V";
			Rule rule = new Rule(ruleStr, cats);
			String output = rule.parse("qipa kita tika");
			assertEquals("qiba kida tiga", output);
			
			ruleStr = "Z/S/V_V";
			rule = new Rule(ruleStr, cats);
			output = rule.parse("qiba kida tiga");
			assertEquals("qipa kita tika", output);
		} catch (InvalidRuleSyntaxException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// blank target
	@Test
	void blankTarget() {
		try {
			String[] vowelVals = {"a", "e", "i", "o", "u"};
			Category vowels = new Category("V", vowelVals);
			ArrayList<Category> cats = new ArrayList<Category>();
			cats.add(vowels);
			
			Rule rule = new Rule("V//V_V", cats);
			String output = rule.parse("pieuw tiuw kəu o");
			assertEquals("piuw tiuw kəu o", output);
		} catch (InvalidRuleSyntaxException e) {
			e.printStackTrace();
			fail();
		}	
	}
	
	// optional elements
	@Test
	void optionalElements() {
		String[] voicelessVals = {"p", "t", "k"};
		String[] voicedVals = {"b", "d", "g"};
		String[] vowelVals = {"a", "e", "i", "o", "u"};
		Category voiceless = new Category("S", voicelessVals);
		Category voiced = new Category("Z", voicedVals);
		Category vowels = new Category("V", vowelVals);
		ArrayList<Category> cats = new ArrayList<Category>();
		cats.add(voiceless);
		cats.add(voiced);
		cats.add(vowels);
		
		try {
			String ruleStr = "S/Z/V(S)_(m)V";
			Rule rule = new Rule(ruleStr, cats);
			String output = rule.parse("qitpa kita tikma");
			assertEquals("qitba kida tigma", output);
		} catch (InvalidRuleSyntaxException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// metathesis
	@Test
	void metathesis() {
		try {
			String[] vowelVals = {"a", "e", "i", "o", "u"};
			Category vowels = new Category("V", vowelVals);
			ArrayList<Category> cats = new ArrayList<Category>();
			cats.add(vowels);
			
			Rule rule = new Rule("Va/\\\\\\\\/_", cats);
			String output = rule.parse("pia pai puoa");
			assertEquals("pai pai puao", output);
		} catch (InvalidRuleSyntaxException e) {
			e.printStackTrace();
			fail();
		}	
	}
	
	// nonce categories
	@Test
	void nonceCategories() {
		try {
			String[] frontVals = {"a", "e", "i"};
			Category front = new Category("F", frontVals);
			ArrayList<Category> cats = new ArrayList<Category>();
			cats.add(front);
			
			String[] backVals = {"o", "u"};
			Category back = new Category("F", backVals);
			cats.add(back);
			
			Rule rule = new Rule("k/g/_[aeiou]", cats);
			String output = rule.parse("akka kke ikki kko ukku");
			assertEquals("akga kge ikgi kgo ukgu", output);
			
			rule = new Rule("k/g/_[FB]", cats);
			output = rule.parse("afka bke ikkə kko ukku");
			assertEquals("afga bge ikkə kgo ukgu", output);
		} catch (InvalidRuleSyntaxException e) {
			e.printStackTrace();
			fail();
		}	
	}
	
	// degemination
	@Test
	void degemination() {
		String[] nasalVals = {"m", "n"};
		Category nasals = new Category("M", nasalVals);
		ArrayList<Category> cats = new ArrayList<Category>();
		cats.add(nasals);
		try {
			Rule rule = new Rule("m//²", cats);
			String output = rule.parse("mmimm nninn mnimn");
			assertEquals("mim nninn mnimn", output);
			
			rule = new Rule("M//_²", cats);
			output = rule.parse("mmimm nninn mnimn");
			assertEquals("mim nin mnimn", output);
		} catch (InvalidRuleSyntaxException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// wildcard
	@Test
	void wildcard() {
		String[] voicelessVals = {"p", "t", "k"};
		String[] voicedVals = {"b", "d", "g"};
		String[] vowelVals = {"a", "e", "i", "o", "u"};
		Category voiceless = new Category("S", voicelessVals);
		Category voiced = new Category("Z", voicedVals);
		Category vowels = new Category("V", vowelVals);
		ArrayList<Category> cats = new ArrayList<Category>();
		cats.add(voiceless);
		cats.add(voiced);
		cats.add(vowels);
		
		try {
			String ruleStr = "S/Z/_…V";
			Rule rule = new Rule(ruleStr, cats);
			String output = rule.parse("ptka");
			assertEquals("btka", output);
			
			output = rule.parse("btka");
			assertEquals("bdka", output);
		} catch (InvalidRuleSyntaxException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// multi-wildcard
		@Test
		void multiWildcard() {
			String[] voicelessVals = {"p", "t", "k"};
			String[] voicedVals = {"b", "d", "g"};
			String[] vowelVals = {"a", "e", "i", "o", "u"};
			Category voiceless = new Category("S", voicelessVals);
			Category voiced = new Category("Z", voicedVals);
			Category vowels = new Category("V", vowelVals);
			ArrayList<Category> cats = new ArrayList<Category>();
			cats.add(voiced);
			cats.add(voiceless);
			cats.add(vowels);
			
			try {
				String ruleStr = "S…V/Z…V/_…Z";
				Rule rule = new Rule(ruleStr, cats);
				String output = rule.parse("pjjjjjajjjjb");
				assertEquals("bjjjjjajjjjb", output);
			} catch (InvalidRuleSyntaxException e) {
				e.printStackTrace();
				fail();
			}
		}
	
	// with gloss
	@Test
	void withGloss() {
		String ruleStr = "t/d/_";
		String input = "tatada ‣ test";
		try {
			Rule rule = new Rule(ruleStr);
			String output = rule.parse(input);
			assertEquals("dadada ‣ test", output);
		} catch (InvalidRuleSyntaxException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	// exceptions
	@Test
	void exceptions() {
		String ruleStr = "t/d/_/#_";
		String input = "tatada";
		try {
			Rule rule = new Rule(ruleStr);
			String output = rule.parse(input);
			assertEquals("tadada", output);
		} catch (InvalidRuleSyntaxException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	
	// load .sc file
	void loadSCFile() {
		
	}
	
	// export to .sc file
	void exportSCFile() {
		
	}

}

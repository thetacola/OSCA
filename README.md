# OSCA
[![Maven Central](https://img.shields.io/maven-central/v/net.oijon/OSCA?style=plastic)](https://search.maven.org/artifact/net.oijon/OSCA)

A sound change applier in Java. OSCA should be compatible with SCA² by zompist, which can be found at https://www.zompist.com/sca2.html. That being said, full compatibility cannot be 100% guaranteed, so use with caution!

## Syntax
Rules can be split into four parts, those being:
- **Target**: What's meant to be replaced
- **Replacement**: What to replace the target with
- **Environment**: When the target should be turned into the replacement. Where the target would fall in this is marked with _
- **Exception**: When the target should *not* be turned into the replacement, but otherwise would with the given environment. Optional element

These rules can be written in the syntax Target/Replacement/Environment/Exception. For example, the rule `p/b/_i/a_` would change 'p' to 'b' before 'i', except after 'a'. A rule can be created with the Rule(String) constructor this way. Alternatively, these can be split apart with Rule(String, String, String, String). Rules can then be applied to strings with Rule.parse(String), which will return the output of that rule. Just about any sound change can be done with a combination of this syntax and **categories**, which can be seen in the next section. For the few edge cases that cannot be, the way to do those are further down.

### Categories
Categories are essentially named placeholders that can be replaced with certain defined characters. In OSCA, a category can be created from either an array or ArrayList via the Category(String, {String[] or ArrayList<String>}) constructors. ArrayList<Category>s can then be added to the end of rule constructors to allow them to use it. For example, let's say there's a category V that contains a, e, i, o, and u. This can be used to make the rule `p/b/V_V`, which turns 'p' into 'b' between vowels. Categories can also map to eachother in rules, allowing the rule `VpV/VbV/_` to have the same behavior. There is one special category, `#`, that represents whitespace and word boundaries. This allows for rules such as `Vi/Vu/_#`, which replaced 'i' with 'u' after a vowel at the end of a word.

### Optional Elements
Sometimes, a rule may apply both with and without a certain element. For example, the rule `a/o/u(V)_` replaces 'a' with 'o' either after 'u' or after 'u' followed by another vowel.

### Degemination
Degemination can be achieved with ² in the environment. For example, if we have a category called N with m and n, `N//_²` will replace 'mm' with 'm', 'nn' with 'n', but keep 'mn' and 'nm' the same.

### Metathesis
Metathesis does not fit the syntax of rules particularly well. Therefore, setting the replacement to '\\' will metathesize the target. For example, `Va/\\/_` will swap the positions of any vowel and 'a' if 'a' originally proceeds the vowel.

### Glossing
Glossing can be done using ‣. Anything in the input after '‣' will be ignored by the rule. This is useful for writing down the meanings of words for example.

### Nonce Categories
Categories can also be made in rules themselves. This can be useful if there's no category containing the elements you need and it's only for one rule, or if the members of multiple categories need to be combined. For example, `k/g/_[aeiou]` has the same result as using `k/g/_V` that was defined earlier. Furthermore, `k/g/_[FB]` will change 'k' to 'g' when followed by any member of category F or category B.

### Wildcards
Wildcards can be used to match any amount of arbitrary characters using '…'. For example, `p/b/_…a` will change 'p' to 'b' if followed at all by 'a'.

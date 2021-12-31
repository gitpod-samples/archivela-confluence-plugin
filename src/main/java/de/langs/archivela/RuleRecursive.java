package de.langs.archivela;

/**
 * Recursive select option. TODO Find a way to use a boolean.
 */
public abstract class RuleRecursive {
	public static final String Yes = "yes";
	public static final String No = "no";

	/**
	 * Validate that a given string is a valid rule recursive option.
	 */
	public static boolean validate(String option) {
		if (RuleRecursive.Yes.equals(option))
			return true;
		if (RuleRecursive.No.equals(option))
			return true;
		return false;
	}
}

package de.langs.archivela;

import java.text.SimpleDateFormat;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.user.UserManager;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;

/**
 * A rule defines how and when the archiving happens. To allow for easy
 * serialization all values are stored as a String. Origin and contribution from
 * Email-to-Confluence.
 */
@Data
@Builder
@JsonDeserialize(builder = Rule.RuleBuilder.class)
public class Rule {
	private String sourceParentPage;
	private String targetParentPage;
	private String jobUser;
	private String prefix;
	private String dateFormat;
	private String recursive;

	@Setter
	@ComponentImport
	private UserManager userManager;

	/**
	 * Check that all fields of the rule are valid.
	 */
	public void validate(UserValidator userVal, PageValidator pageVal) throws RuleValidationException {
		userVal.validate(jobUser);
		pageVal.validate(sourceParentPage);
		pageVal.validate(targetParentPage);

		if (prefix != null && !prefix.isEmpty() && !prefix.matches("^[a-zA-Z0-9\\-_]*$")) {
			throw new RuleValidationException("Prefix is empty or does not match allowed pattern '^[a-zA-Z0-9\\-_]*$'");
		}

		if (dateFormat != null && dateFormat.isEmpty()) {
			throw new RuleValidationException("Date format is empty");
		}
		try {
			new SimpleDateFormat(dateFormat);
		} catch (Exception e) {
			throw new RuleValidationException("Date format is invalid");
		}

		if (!RuleRecursive.validate(recursive)) {
			// should not happen
			throw new RuleValidationException("Recursive option is invalid");
		}
	}

	@JsonPOJOBuilder(buildMethodName = "build", withPrefix = "")
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class RuleBuilder {
	}
}

package de.langs.archivela;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.user.EntityException;
import com.atlassian.user.UserManager;

import lombok.NonNull;

/**
 * Check if the given user is valid.
 */
@Component
public class UserValidator {
	private UserManager userManager;

	@Autowired
	public UserValidator(@ComponentImport @NonNull UserManager userManager) {
		this.userManager = userManager;
	}

	public void validate(String jobUser) throws RuleValidationException {
		try {
			if (jobUser == null || jobUser.isEmpty()) {
				throw new RuleValidationException("No user given");
			} else if (userManager.getUser(jobUser) == null) {
				throw new RuleValidationException("Invalid user");
			}
		} catch (EntityException e) {
			throw new RuleValidationException("Error during validation");
		}
	}
}

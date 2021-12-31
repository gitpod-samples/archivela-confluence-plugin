package de.langs.archivela;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Check if the given page name is valid.
 */
@Component
public class PageValidator {

	@Autowired
	public PageValidator() {
	}

	public void validate(String pageName) throws RuleValidationException {
		if (pageName == null || pageName.isEmpty()) {
			throw new RuleValidationException("No page given");
		} else if (!(pageName.contains("(") && pageName.contains(")"))) {
			throw new RuleValidationException("Wrong format of page name, should be 'Title (SpaceKey)'");
		} else if (PageUtils.getPage(pageName) == null) {
			throw new RuleValidationException("Non-accessible page");
		}
	}
}

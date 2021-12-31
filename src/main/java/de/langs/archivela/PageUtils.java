package de.langs.archivela;

import com.atlassian.confluence.pages.Page;

/**
 * Utilities for working with pages.
 */
public class PageUtils {

	/**
	 * Get the page given the page name from input fields.
	 * 
	 * @param pageNameWithSpace page name in form 'Title (SpaceKey)'
	 * @return found page or null if none could be found
	 */
	public static Page getPage(String pageNameWithSpace) {
		String pageNameWithSpaceTrimmed = pageNameWithSpace.trim();

		String pageName = pageNameWithSpaceTrimmed.substring(0, pageNameWithSpaceTrimmed.lastIndexOf('(')).trim();

		String spaceKey = pageNameWithSpaceTrimmed
				.substring(pageNameWithSpaceTrimmed.lastIndexOf('(') + 1, pageNameWithSpaceTrimmed.lastIndexOf(')'))
				.trim();

		// TODO Is an exception already thrown beforehand?
		if (spaceKey.isEmpty() || pageName.isEmpty()) {
			return null;
		} else {
			return StaticAccessor.getPageManager().getPage(spaceKey, pageName);
		}
	}
}

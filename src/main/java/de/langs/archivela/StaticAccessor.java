package de.langs.archivela;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import lombok.Getter;
import lombok.Setter;

/**
 * Some component imports don't work in jobs or in tests. But can be accessed
 * through static properties. See:
 * https://developer.atlassian.com/server/framework/atlassian-sdk/access-components-statically/.
 * Origin and contribution from Email-to-Confluence.
 */
@Component
public class StaticAccessor {
	private static @Getter @Setter UserAccessor userAccessor;

	private static @Getter @Setter PageManager pageManager;

	@Autowired
	public StaticAccessor(@ComponentImport UserAccessor userAccessor, @ComponentImport PageManager pageManager) {
		setUserAccessor(userAccessor);
		setPageManager(pageManager);
	}
}

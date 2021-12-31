package de.langs.archivela;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Setter;

/**
 * Autowired bean that stores the configuration currently used in the settings
 * form. Origin and contribution from Email-to-Confluence.
 */
@Component
public class ConfigurationActionState {

	@Setter
	@Autowired
	GlobalState globalState;

	@Setter
	Configuration configuration;

	/**
	 * Get the configuration currently being edited or duplicate the actively used
	 * one.
	 *
	 * @return the configuration currently being edited
	 */
	public Configuration getConfiguration() {
		if (configuration == null) {
			configuration = globalState.getConfiguration().duplicate();
		}

		return configuration;
	}
}

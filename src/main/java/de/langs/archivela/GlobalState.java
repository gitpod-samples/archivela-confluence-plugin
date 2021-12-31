package de.langs.archivela;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Setter;

/**
 * Autowired bean that stores the plugin wide state. Origin and contribution
 * from Email-to-Confluence.
 */
@Component
public class GlobalState {

	@Setter
	@Autowired
	private ConfigurationManager configurationManager;

	/**
	 * The currently used configuration
	 */
	@Setter
	Configuration configuration = null;

	/**
	 * Get the configuration or lazy load the current active configuration from
	 * storage.
	 *
	 * @return the configuration currently used for processing
	 */
	public Configuration getConfiguration() {
		if (configuration == null) {
			configuration = configurationManager.loadConfig();
		}

		return configuration;
	}
}

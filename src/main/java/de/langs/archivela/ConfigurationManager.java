package de.langs.archivela;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.spring.container.ContainerManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Autowired object that stores the configuration in use and that can load/store
 * the configuration in the Confluence bandana. Origin and contribution from
 * Email-to-Confluence.
 */
@Slf4j
@Component
public class ConfigurationManager {

	@ComponentImport
	private final PluginSettingsFactory pluginSettingsFactory;

	@Inject
	public ConfigurationManager(PluginSettingsFactory pluginSettingsFactory) {
		this.pluginSettingsFactory = pluginSettingsFactory;
	}

	/**
	 * Load configuration from bandana.
	 * 
	 * @return saved Configuration
	 */
	public Configuration loadConfig() {
		Configuration configuration = null;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ConfluenceBandanaContext ctx = newGlobalConfluenceBandaContext();
			Object object = getBandanaManager().getValue(ctx, Constants.PLUGIN_KEY);
			if (object instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) object;
				configuration = objectMapper.convertValue(map, Configuration.class);
			} else if (object != null) {
				log.error(Constants.PLUGIN_KEY + " Failed to load configuration, unexpected type returned");
			}
		} catch (Exception e) {
			configuration = null;
			log.error(Constants.PLUGIN_KEY + " Failed to load configuration", e);
		}

		if (configuration == null) {
			configuration = Configuration.builder().build();
		}

		return configuration;
	}

	/**
	 * Save the configuration to bandana.
	 * 
	 * @param configuration to save
	 */
	public void saveConfig(@NonNull Configuration configuration) throws ConfigurationManagerException {
		try {
			ConfluenceBandanaContext ctx = newGlobalConfluenceBandaContext();
			ObjectMapper objectMapper = new ObjectMapper();
			@SuppressWarnings("unchecked")
			Map<String, Object> map = objectMapper.convertValue(configuration, Map.class);
			getBandanaManager().setValue(ctx, Constants.PLUGIN_KEY, map);
		} catch (Exception e) {
			throw new ConfigurationManagerException("Failed to save configuration", e);
		}

		// Read back the configuration from storage and make sure that it equals the
		// given config.
		if (!configuration.equals(loadConfig())) {
			throw new ConfigurationManagerException("Failed to save configuration");
		}

	}

	// PluginSettingsManager seems to be broken in newer confluence versions.
	// This is why bandana manager is used directly.
	// Couldn't get Autowiring to work for BandanaManager.
	public BandanaManager getBandanaManager() {
		return (BandanaManager) ContainerManager.getComponent("bandanaManager");
	}

	public ConfluenceBandanaContext newGlobalConfluenceBandaContext() {
		return new ConfluenceBandanaContext((Space) null);
	}
}

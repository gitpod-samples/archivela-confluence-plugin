package de.langs.archivela;

import org.springframework.beans.factory.annotation.Autowired;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Action for configuration save. Origin and contribution from
 * Email-to-Confluence.
 */
@SuppressWarnings("serial")
@Slf4j
public class ConfigurationAction extends ConfluenceActionSupport {

	@Setter
	@Autowired
	private ConfigurationManager configurationManager;

	@Setter
	@Autowired
	private GlobalState globalState;

	@Setter
	@ComponentImport
	private SpaceManager spaceManager;

	@Setter
	@Autowired
	private ConfigurationActionState configurationActionState;

	@Setter
	@Autowired
	private UserValidator userValidator;

	@Setter
	@Autowired
	private PageValidator pageValidator;

	/**
	 * Strings for xworks deserialization. Variable names map to the name of the
	 * input fields in the VM.
	 */
	@Setter
	private String[] ruleJobUsers = new String[] {};
	@Setter
	private String[] ruleSourceParentPages = new String[] {};
	@Setter
	private String[] ruleTargetParentPages = new String[] {};
	@Setter
	private String[] rulePrefixes = new String[] {};
	@Setter
	private String[] ruleDateFormats = new String[] {};
	@Setter
	private String[] ruleRecursives = new String[] {};

	/**
	 * Triggered when the user accesses the edit form for the first time.
	 */
	public String doDefault() {
		return ConfluenceActionSupport.INPUT;
	}

	/**
	 * Get the configuration currently being edited. This method is called by xworks
	 * from the template (e.g. when accessing configuration.rules).
	 */
	public Configuration getConfiguration() {

		return configurationActionState.getConfiguration();
	}

	/**
	 * Validate form. Output errors when input is not as expected.
	 */
	public void validate() {

		super.validate();

		Rule[] rules = new Rule[ruleSourceParentPages.length];

		if ((ruleSourceParentPages.length != ruleTargetParentPages.length)
				|| (ruleSourceParentPages.length != ruleJobUsers.length)
				|| (ruleSourceParentPages.length != rulePrefixes.length)
				|| (ruleSourceParentPages.length != ruleDateFormats.length)
				|| (ruleSourceParentPages.length != ruleRecursives.length)) {
			addActionError("Invalid rules due to count inconsistency");
			addFieldError("configuration.rules", "Invalid rules due to count inconsistency");
		} else {

			for (int i = 0; i < ruleSourceParentPages.length; i++) {
				String user = ruleJobUsers[i];
				String source = ruleSourceParentPages[i];
				String target = ruleTargetParentPages[i];
				String prefix = rulePrefixes[i];
				String dateFormat = ruleDateFormats[i];
				String recursive = ruleRecursives[i];

				// Create rule
				Rule rule = Rule.builder().jobUser(user).sourceParentPage(source).targetParentPage(target)
						.prefix(prefix).dateFormat(dateFormat).recursive(recursive).build();

				try {
					// Validate rule
					rule.validate(userValidator, pageValidator);
				} catch (RuleValidationException e) {
					addActionError("Rule " + (i + 1) + ": " + e.getLocalizedMessage());
					addFieldError("configuration.rules", "Rule " + (i + 1) + ": " + e.getLocalizedMessage());
				}

				rules[i] = rule;
			}

			getConfiguration().setRules(rules);
		}

		ruleJobUsers = new String[] {};
		ruleSourceParentPages = new String[] {};
		ruleTargetParentPages = new String[] {};
		rulePrefixes = new String[] {};
		ruleDateFormats = new String[] {};
		ruleRecursives = new String[] {};
	}

	/**
	 * Process submitted values. Save the configuration to configuration manager and
	 * global state i.e. save it for good.
	 */
	public String execute() throws Exception {
		try {
			// Save the configuration and update the global state
			configurationManager.saveConfig(getConfiguration());
			globalState.setConfiguration(getConfiguration().duplicate());
			addActionMessage("Configuration successfully saved.");
			return ConfluenceActionSupport.SUCCESS;
		} catch (ConfigurationManagerException e) {
			addActionError("Failed to save configuration.");
			log.error(Constants.PLUGIN_KEY + " Failed to save configuration", e);
			return ConfluenceActionSupport.ERROR;
		}
	}
}

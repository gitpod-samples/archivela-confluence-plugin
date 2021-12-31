package de.langs.archivela;

import com.atlassian.xwork.ParameterSafe;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * Bean that stores the plugin's configuration. Origin and contribution from
 * Email-to-Confluence.
 */
@Builder(toBuilder = true)
@ParameterSafe // https://developer.atlassian.com/confdev/confluence-plugin-guide/confluence-plugin-module-types/xwork-webwork-module/xwork-plugin-complex-parameters-and-security
@JsonDeserialize(builder = Configuration.ConfigurationBuilder.class)
@EqualsAndHashCode
@Data
public class Configuration {

	@NonNull
	@Getter(onMethod = @__({ @ParameterSafe }))
	private Rule[] rules;

	// Builder class with default values.
	@JsonPOJOBuilder(buildMethodName = "build", withPrefix = "")
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ConfigurationBuilder {
		private Rule[] rules = new Rule[] {};
	}

	public Configuration duplicate() {
		return toBuilder().build();
	}
}

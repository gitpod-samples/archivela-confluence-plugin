package de.langs.archivela;

public class RuleValidationException extends Exception {
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;

	public RuleValidationException() {
	}

	public RuleValidationException(String message) {
		super(message);
	}

	public RuleValidationException(Throwable cause) {
		super(cause);
	}

	public RuleValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}

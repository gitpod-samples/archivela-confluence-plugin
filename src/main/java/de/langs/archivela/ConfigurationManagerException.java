package de.langs.archivela;

@SuppressWarnings("serial")
public class ConfigurationManagerException extends Exception {

    public ConfigurationManagerException() {
    }

    public ConfigurationManagerException(String message) {
        super(message);
    }

    public ConfigurationManagerException(Throwable cause) {
        super(cause);
    }

    public ConfigurationManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}

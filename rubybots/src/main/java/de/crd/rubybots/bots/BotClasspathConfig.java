package de.crd.rubybots.bots;

public class BotClasspathConfig implements BotConfig {

	private final String classpathReference;

	public BotClasspathConfig(String classpathReference) {
		this.classpathReference = classpathReference;
	}

	public String getClasspathReference() {
		return classpathReference;
	}

}

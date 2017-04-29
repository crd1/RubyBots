package de.crd.rubybots.bots;

import java.io.File;

public class BotFileConfig implements BotConfig {

	private final File botFile;

	public BotFileConfig(File botFile) {
		this.botFile = botFile;
	}

	public File getBotFile() {
		return botFile;
	}

	@Override
	public String getName() {
		return botFile.getName();
	}

}

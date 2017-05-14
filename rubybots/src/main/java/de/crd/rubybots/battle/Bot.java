package de.crd.rubybots.battle;

import de.crd.rubybots.bots.BotConfig;

public class Bot implements BattlefieldEntity {

	private final int botNumber;
	private final BotConfig botConfig;

	public Bot(int botNumber, BotConfig botConfig) {
		this.botNumber = botNumber;
		this.botConfig = botConfig;
	}

	public int getBotNumber() {
		return botNumber;
	}

	public BotConfig getBotConfig() {
		return botConfig;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + botNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bot other = (Bot) obj;
		if (botNumber != other.botNumber)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Bot [botNumber=" + botNumber + "]";
	}

}

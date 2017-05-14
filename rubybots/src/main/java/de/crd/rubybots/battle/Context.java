package de.crd.rubybots.battle;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.crd.rubybots.battle.Battlefield.BattlefieldView;

public class Context {

	private static final Logger LOGGER = Logger.getLogger(Context.class.getSimpleName());
	private final Battle battle;
	private final int botNumber;
	private final int round; // note that this is counted from 1 onwards
	private final BattlefieldView battlefield;
	private final int numberOfBots;

	public Context(Battle battle, int botNumber, int round, BattlefieldView battlefieldView, int numberOfBots) {
		this.battle = battle;
		this.botNumber = botNumber;
		this.round = round;
		this.battlefield = battlefieldView;
		this.numberOfBots = numberOfBots;
	}

	public int getBotNumber() {
		return botNumber;
	}

	public int getRound() {
		return round;
	}

	public BattlefieldView getBattlefield() {
		return battlefield;
	}
	
	public void storeData(Object data) {
		this.battle.getBot(botNumber).storeData(data);
	}
	
	public Object getStoredData() {
		return this.battle.getBot(botNumber).getStoredData();
	}

	public int getNumberOfBots() {
		return numberOfBots;
	}

	public void log(String log) {
		LOGGER.log(Level.FINE, "Bot " + botNumber + ": " + log);
	}

	@Override
	public String toString() {
		return "Context [botNumber=" + botNumber + ", round=" + round + ", battlefieldView=" + battlefield
				+ ", numberOfBots=" + numberOfBots + "]";
	}

}

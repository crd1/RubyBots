package de.crd.rubybots.battle;

import de.crd.rubybots.battle.Battlefield.BattlefieldView;

public class Context {

	private final int botNumber;
	private final int round; // note that this is counted from 1 onwards
	private final BattlefieldView battlefield;
	private final int numberOfBots;

	public Context(int botNumber, int round, BattlefieldView battlefieldView, int numberOfBots) {
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

	public int getNumberOfBots() {
		return numberOfBots;
	}

	@Override
	public String toString() {
		return "Context [botNumber=" + botNumber + ", round=" + round + ", battlefieldView=" + battlefield
				+ ", numberOfBots=" + numberOfBots + "]";
	}

}

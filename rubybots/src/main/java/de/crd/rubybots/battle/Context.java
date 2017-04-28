package de.crd.rubybots.battle;

public class Context {

	private final int botNumber;
	private final int round; // note that this is counted from 1 onwards
	private final Battlefield battlefield;
	private final int numberOfBots;

	public Context(int botNumber, int round, Battlefield battlefield, int numberOfBots) {
		this.botNumber = botNumber;
		this.round = round;
		this.battlefield = battlefield;
		this.numberOfBots = numberOfBots;
	}

	public int getBotNumber() {
		return botNumber;
	}

	public int getRound() {
		return round;
	}

	public Battlefield getBattlefield() {
		return battlefield;
	}

	public int getNumberOfBots() {
		return numberOfBots;
	}

	@Override
	public String toString() {
		return "Context [botNumber=" + botNumber + ", round=" + round + ", battlefield=" + battlefield + "]";
	}

}

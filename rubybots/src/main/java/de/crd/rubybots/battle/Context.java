package de.crd.rubybots.battle;

public class Context {

	private final int botNumber;
	private final int round;

	public Context(int botNumber, int round) {
		this.botNumber = botNumber;
		this.round = round;
	}

	public int getBotNumber() {
		return botNumber;
	}

	public int getRound() {
		return round;
	}

	@Override
	public String toString() {
		return "Context [botNumber=" + botNumber + ", round=" + round + "]";
	}

}

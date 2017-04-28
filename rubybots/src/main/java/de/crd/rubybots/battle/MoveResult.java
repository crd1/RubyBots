package de.crd.rubybots.battle;

public class MoveResult {

	private int botNumber = 0;

	@Override
	public String toString() {
		return "MoveResult [botNumber=" + botNumber + "]";
	}

	public int getBotNumber() {
		return botNumber;
	}

	public void setBotNumber(int botNumber) {
		this.botNumber = botNumber;
	}

}

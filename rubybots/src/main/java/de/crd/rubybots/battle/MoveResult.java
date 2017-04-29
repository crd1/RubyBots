package de.crd.rubybots.battle;

import java.util.ArrayList;
import java.util.List;

public class MoveResult {

	private int botNumber = 0;
	private final List<Action> actions = new ArrayList<>();

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

	public List<Action> getActions() {
		return actions;
	}

}

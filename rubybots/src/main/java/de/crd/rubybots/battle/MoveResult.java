package de.crd.rubybots.battle;

import java.util.ArrayList;
import java.util.List;

public class MoveResult {

	private final int botNumber;
	private final List<Action> actions = new ArrayList<>();

	public MoveResult(int botNumber) {
		this.botNumber = botNumber;
	}

	@Override
	public String toString() {
		return "MoveResult [botNumber=" + botNumber + "]";
	}

	public int getBotNumber() {
		return botNumber;
	}

	public List<Action> getActions() {
		return actions;
	}

}

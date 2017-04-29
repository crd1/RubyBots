package de.crd.rubybots.battle;

public class Action {

	private final ActionType actionType;
	private final int botNumber;

	public Action(int botNumber, ActionType actionType) {
		this.actionType = actionType;
		this.botNumber = botNumber;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public enum ActionType {
		MOVE;
	}

	public int getBotNumber() {
		return botNumber;
	}

}

package de.crd.rubybots.battle;

public class Action {

	private final ActionType actionType;
	private final int botNumber;
	private final Integer targetPosition;

	public Action(int botNumber, Integer targetPosition, ActionType actionType) {
		this.actionType = actionType;
		this.targetPosition = targetPosition;
		this.botNumber = botNumber;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public enum ActionType {
		MOVE, FIRE;
	}

	public int getBotNumber() {
		return botNumber;
	}

	public Integer getTargetPosition() {
		return targetPosition;
	}

}

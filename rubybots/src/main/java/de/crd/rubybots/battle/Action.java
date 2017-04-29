package de.crd.rubybots.battle;

public class Action {

	private final ActionType actionType;

	public Action(ActionType actionType) {
		this.actionType = actionType;
	}

	public enum ActionType {
		MOVE;
	}

}

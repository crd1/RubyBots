package de.crd.rubybots.battle;

import de.crd.rubybots.battle.Action.ActionType;

public class Battlefield {

	public BattlefieldView toView() {
		return new BattlefieldView();
	}

	public void applyMoveResult(MoveResult result) {
		// TODO
	}

	public static MoveResult extractMoveResult(BattlefieldView battlefieldView) {
		return battlefieldView.moveResult;
	}

	@Override
	public String toString() {
		return "Battlefield []";
	}

	/**
	 * Determines whether this battle has already been won by some bot.
	 * 
	 */
	public boolean isOwned() {
		// TODO
		return false;
	}

	public BattleStats getBattleStats() {
		// TODO
		return new BattleStats();
	}

	public static class BattlefieldView {
		private final MoveResult moveResult = new MoveResult();

		public void move() {
			this.moveResult.getActions().add(new Action(ActionType.MOVE));
		}
	}
}
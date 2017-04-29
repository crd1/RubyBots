package de.crd.rubybots.battle;

import de.crd.rubybots.battle.Action.ActionType;

public class Battlefield {

	private final Battle parentBattle;
	private int currentRound;

	public Battlefield(Battle parentBattle) {
		this.parentBattle = parentBattle;
	}

	public int getCurrentRound() {
		return currentRound;
	}

	public int nextRound() {
		return ++currentRound;
	}

	public BattlefieldView toView() {
		return new BattlefieldView();
	}

	public void applyMoveResult(MoveResult result) {
		// TODO
	}

	/**
	 * This mechanism serves to prevent the bot from manipulating the moveResult
	 * directly but to publish the result anyway.
	 */
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
		return getWinner() != null;
	}

	public BattleStats getBattleStats() {
		return BattleStats.calculateStats(parentBattle.getStartTime(), this);
	}

	public static class BattlefieldView {
		private final MoveResult moveResult = new MoveResult();

		public void move() {
			this.moveResult.getActions().add(new Action(ActionType.MOVE));
		}
	}

	public Integer getWinner() {
		// TODO
		return null;
	}
}
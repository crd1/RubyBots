package de.crd.rubybots.battle;

public class Battlefield {

	public Battlefield immutableCopy() {
		return new Battlefield();
	}

	public void applyMoveResult(MoveResult result) {
		// TODO
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
}
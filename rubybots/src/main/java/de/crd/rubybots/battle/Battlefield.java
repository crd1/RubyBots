package de.crd.rubybots.battle;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.crd.rubybots.battle.Action.ActionType;

public class Battlefield {

	private static final int SPACE_PER_BOT = 5;
	private final Battle parentBattle;
	private int currentRound;
	private final Map<Integer, Integer> field;
	private final int fieldSize;

	public Battlefield(Battle parentBattle) {
		this.field = new HashMap<>();
		this.parentBattle = parentBattle;
		this.fieldSize = getFieldSize(parentBattle.getNumberOfBots());
		initField(parentBattle.getNumberOfBots());
	}

	public Battlefield(Battlefield toCopy) {
		this.parentBattle = toCopy.parentBattle;
		this.currentRound = toCopy.currentRound;
		this.field = new HashMap<>(toCopy.field);
		this.fieldSize = toCopy.fieldSize;
	}

	private int getFieldSize(int numberOfBots) {
		return numberOfBots * SPACE_PER_BOT;
	}

	/**
	 * Sets initial positions for the bots.
	 */
	private void initField(final int numberOfBots) {
		for (int i = 0; i < numberOfBots; i++) {
			field.put(i * SPACE_PER_BOT, i);
		}
	}

	public int getCurrentRound() {
		return currentRound;
	}

	public int nextRound() {
		return ++currentRound;
	}

	public BattlefieldView toView(int botNumber) {
		return new BattlefieldView(botNumber);
	}

	public void applyAction(Action action) {
		Integer currentPositionOfBot = getPosition(action.getBotNumber());
		if (currentPositionOfBot == null) {
			System.out.println("Bot has already been destroyed. Skipping action for bot " + action.getBotNumber());
			return;
		}
		switch (action.getActionType()) {
		case MOVE:
			move(action.getBotNumber(), currentPositionOfBot);
			break;
		default:
			System.out.println("Unhandled action: " + action.getActionType());
			break;
		}
	}

	private void move(int botNumber, int currentPosition) {
		Integer nextFree = getNextFreePosition(currentPosition);
		if (nextFree == null) {
			System.out.println("No position to move to. Skipping MOVE.");
			return;
		}
		// move the bot
		field.put(currentPosition, null);
		field.put(nextFree, botNumber);
	}

	private Integer getNextFreePosition(int currentPosition) {
		for (int i = currentPosition + 1; i != currentPosition; i++) {
			if (i == fieldSize) {
				// overflow - this field is circular
				i = -1;
				continue;
			}
			if (field.get(i) == null) {
				return i;
			}
		}
		return null;
	}

	private Integer getPosition(int botNumber) {
		for (Entry<Integer, Integer> entry : field.entrySet()) {
			if (Integer.valueOf(botNumber).equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
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
		return "Battlefield [currentRound=" + currentRound + ", field=" + getFieldRepresentation() + "]";
	}

	private String getFieldRepresentation() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fieldSize; i++) {
			Integer botOnField = field.get(i);
			sb.append(botOnField != null ? botOnField : "*");
		}
		return sb.toString();
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
		private final MoveResult moveResult;

		public BattlefieldView(int botNumber) {
			this.moveResult = new MoveResult(botNumber);
		}

		public void move() {
			this.moveResult.getActions().add(new Action(moveResult.getBotNumber(), ActionType.MOVE));
		}

		@Override
		public String toString() {
			return "BattlefieldView [moveResult=" + moveResult + "]";
		}
	}

	public Integer getWinner() {
		Set<Entry<Integer, Integer>> entrySet = field.entrySet();
		if (entrySet.size() > 0) {
			return null;
		}
		return entrySet.iterator().next().getValue(); // last man standing...
	}

}
package de.crd.rubybots.battle;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.crd.rubybots.battle.Action.ActionType;

public class Battlefield {

	private static final int SPACE_PER_BOT = 5;
	private final Battle parentBattle;
	private int currentRound;
	private final Map<Integer, Integer> field;
	private final Map<Integer, Map<ActionType, Integer>> history = new HashMap<>();
	private final int fieldSize;

	public Battlefield(Battle parentBattle) {
		this.field = new HashMap<>();
		this.parentBattle = parentBattle;
		this.fieldSize = getFieldSize(parentBattle.getNumberOfBots());
		initField(parentBattle.getNumberOfBots());
		this.initHistory(parentBattle.getNumberOfBots());
	}

	public Battlefield(Battlefield toCopy) {
		this.parentBattle = toCopy.parentBattle;
		this.currentRound = toCopy.currentRound;
		this.field = new HashMap<>(toCopy.field);
		for (Entry<Integer, Map<ActionType, Integer>> entry : toCopy.history.entrySet()) {
			this.history.put(entry.getKey(), new HashMap<>(entry.getValue()));
		}
		this.fieldSize = toCopy.fieldSize;
	}

	private void initHistory(int numberOfBots) {
		for (int i = 0; i < numberOfBots; i++) {
			history.put(i, new HashMap<>());
		}
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
		return new BattlefieldView(botNumber, fieldSize, field);
	}

	public void applyAction(Action action) {
		Integer currentPositionOfBot = getPosition(action.getBotNumber());
		if (currentPositionOfBot == null) {
			System.out.println("Bot has already been destroyed. Skipping action for bot " + action.getBotNumber());
			return;
		}
		System.out.println("Applying action " + action.getActionType() + " for bot " + action.getBotNumber());
		switch (action.getActionType()) {
		case MOVE:
			move(action.getBotNumber(), currentPositionOfBot);
			break;
		case FIRE:
			fire(action.getBotNumber(), action.getTargetPosition());
			break;
		default:
			System.out.println("Unhandled action: " + action.getActionType());
			break;
		}
		Integer thisBotsActionsBefore = history.get(action.getBotNumber()).get(action.getActionType());
		if (thisBotsActionsBefore == null) {
			thisBotsActionsBefore = 0;
		}
		history.get(action.getBotNumber()).put(action.getActionType(), thisBotsActionsBefore + 1);
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

	private void fire(int botNumber, int targetPosition) {
		if (targetPosition >= 0 && targetPosition < fieldSize) {
			Integer botFiredAt = field.get(targetPosition);
			field.remove(targetPosition);
			if (botFiredAt != null && botFiredAt == botNumber) {
				System.out.println("Bot " + botNumber + " commited suicide.");
			} else if (botFiredAt != null && botFiredAt != botNumber) {
				System.out.println("Bot " + botNumber + " destroyed bot " + botFiredAt);
			}
		} else {
			System.out.println("Bot " + botNumber + " fired outside of field.");
		}
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

	public Map<Integer, Map<ActionType, Integer>> getHistory() {
		return history;
	}

	public BattleStats getBattleStats() {
		return BattleStats.calculateStats(parentBattle.getStartTime(), parentBattle.getNumberOfBots(), this);
	}

	public static class BattlefieldView {
		private final MoveResult moveResult;
		private final int size;
		private final Map<Integer, Integer> _field;

		public BattlefieldView(int botNumber, int battlefieldSize, Map<Integer, Integer> field) {
			this.size = battlefieldSize;
			this.moveResult = new MoveResult(botNumber);
			this._field = new HashMap<>(field);
		}

		public void move() {
			this.moveResult.getActions().add(new Action(moveResult.getBotNumber(), null, ActionType.MOVE));
		}

		public void fire(int targetPosition) {
			this.moveResult.getActions().add(new Action(moveResult.getBotNumber(), targetPosition, ActionType.FIRE));
		}

		public int getSize() {
			return size;
		}

		public Integer whoIsAtPosition(int position) {
			return _field.get(position);
		}

		@Override
		public String toString() {
			return "BattlefieldView [moveResult=" + moveResult + ", size=" + size + "]";
		}

	}

	public Integer getWinner() {
		List<Integer> menStanding = field.values().stream().filter(bot -> bot != null).collect(Collectors.toList());
		Collections.sort(menStanding);
		if (menStanding.size() != 1) {
			System.out.println("No winner. Men standing are: " + menStanding);
			return null;
		}
		return menStanding.get(0); // last man standing...
	}

}
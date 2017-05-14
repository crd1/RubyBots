package de.crd.rubybots.battle;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.crd.rubybots.battle.Action.ActionType;
import de.crd.rubybots.config.Constants;

public class Battlefield {

	private static final Logger LOGGER = Logger.getLogger(Battlefield.class.getSimpleName());
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
		return numberOfBots * Constants.SPACE_PER_BOT;
	}

	/**
	 * Sets initial positions for the bots.
	 */
	private void initField(final int numberOfBots) {
		Random random = new Random();
		for (int i = 0; i < numberOfBots; i++) {
			int position = random.nextInt(fieldSize);
			while (field.get(position) != null) {
				position++;
				if (position >= fieldSize) {
					position = 0;
				}
			}
			field.put(position, i);
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

	public Battle getParentBattle() {
		return parentBattle;
	}

	public void applyAction(Action action) {
		Integer currentPositionOfBot = getPosition(action.getBotNumber());
		if (currentPositionOfBot == null) {
			LOGGER.log(Level.FINE, "Bot has already been destroyed. Skipping action for bot " + action.getBotNumber());
			return;
		}
		LOGGER.log(Level.FINE, "Applying action " + action.getActionType() + " for bot " + action.getBotNumber());
		switch (action.getActionType()) {
		case MOVE:
			move(action.getBotNumber(), currentPositionOfBot);
			break;
		case FIRE:
			fire(action.getBotNumber(), action.getTargetPosition());
			break;
		case SET_MINE:
			mine(action.getBotNumber(), action.getTargetPosition());
			break;
		default:
			LOGGER.log(Level.SEVERE, "Unhandled action: " + action.getActionType());
			break;
		}
		Integer thisBotsActionsBefore = history.get(action.getBotNumber()).get(action.getActionType());
		if (thisBotsActionsBefore == null) {
			thisBotsActionsBefore = 0;
		}
		history.get(action.getBotNumber()).put(action.getActionType(), thisBotsActionsBefore + 1);
	}

	private void mine(int botNumber, Integer targetPosition) {
		if (field.get(targetPosition) == null) {
			field.put(targetPosition, Constants.MINE_REPRESENTATION);
		} else {
			LOGGER.log(Level.FINE, "Mining positions that are taken not possible. Skipping SET_MINE.");
		}
	}

	private void move(int botNumber, int currentPosition) {
		Integer nextFree = getNextFreePosition(currentPosition, true);
		if (nextFree == null) {
			LOGGER.log(Level.FINE, "No position to move to. Skipping MOVE.");
			return;
		}
		// move the bot
		field.put(currentPosition, null);
		if (field.get(nextFree) == null || field.get(nextFree) != Constants.MINE_REPRESENTATION) {
			field.put(nextFree, botNumber);
		} else {
			LOGGER.log(Level.FINE, "Bot " + botNumber + " stepped on a mine.");
			field.put(nextFree, null);
		}
	}

	private void fire(int botNumber, int targetPosition) {
		if (targetPosition >= 0 && targetPosition < fieldSize) {
			Integer firedAt = field.get(targetPosition);
			if (firedAt == null) {
				return;
			}
			field.remove(targetPosition);
			if (firedAt == botNumber) {
				LOGGER.log(Level.FINE, "Bot " + botNumber + " commited suicide.");
			} else if (firedAt == Constants.MINE_REPRESENTATION) {
				LOGGER.log(Level.FINE, "Bot " + botNumber + " destroyed a mine.");
			} else {
				LOGGER.log(Level.FINE, "Bot " + botNumber + " destroyed bot " + firedAt);
			}
		} else {
			LOGGER.log(Level.FINE, "Bot " + botNumber + " fired outside of field.");
		}
	}

	private Integer getNextFreePosition(int currentPosition, boolean includeMined) {
		for (int i = currentPosition + 1; i != currentPosition; i++) {
			if (i == fieldSize) {
				// overflow - this field is circular
				i = -1;
				continue;
			}
			if (field.get(i) == null) {
				return i;
			}
			if (includeMined && field.get(i) == Constants.MINE_REPRESENTATION) {
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

	public String getFieldRepresentation() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fieldSize; i++) {
			Integer botOnField = field.get(i);
			if (botOnField == null) {
				sb.append("-");
			} else if (botOnField == Constants.MINE_REPRESENTATION) {
				sb.append("#");
			} else {
				sb.append(botOnField);
			}
		}
		return sb.toString();
	}

	/**
	 * Determines whether this battle has already been won by some bot or has
	 * ended.
	 * 
	 */
	public boolean isOwned() {
		return getNumberOfMenStanding() == 0 || getWinner() != null;
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

		public void mine(int targetPosition) {
			this.moveResult.getActions()
					.add(new Action(moveResult.getBotNumber(), targetPosition, ActionType.SET_MINE));
		}

		public int getSize() {
			return size;
		}

		public Integer getMyPosition() {
			for (Entry<Integer, Integer> entry : _field.entrySet()) {
				if (Integer.valueOf(moveResult.getBotNumber()).equals(entry.getValue())) {
					return entry.getKey();
				}
			}
			return null;
		}

		public Integer whoIsAtPosition(int position) {
			return _field.get(position);
		}

		@Override
		public String toString() {
			return "BattlefieldView [moveResult=" + moveResult + ", size=" + size + "]";
		}

	}

	public Integer getNumberOfMenStanding() {
		List<Integer> menStanding = field.values().stream()
				.filter(bot -> (bot != null && bot != Constants.MINE_REPRESENTATION)).collect(Collectors.toList());
		return menStanding.size();
	}

	public Integer getWinner() {
		List<Integer> menStanding = field.values().stream()
				.filter(bot -> (bot != null && bot != Constants.MINE_REPRESENTATION)).collect(Collectors.toList());
		Collections.sort(menStanding);
		if (menStanding.size() != 1) {
			// LOGGER.log(Level.FINE, "No winner. Men standing are: " +
			// menStanding);
			return null;
		}
		return menStanding.get(0); // last man standing...
	}

	public boolean isBotAlive(int bot) {
		for(Integer field : this.field.values()) {
			if(field != null && field.equals(bot)) {
				return true;
			}
		}
		return false;
	}

}
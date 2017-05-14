package de.crd.rubybots.battle;

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
	private final Map<Integer, BattlefieldEntity> field;
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
			field.put(position, parentBattle.getBot(i));
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
			field.put(targetPosition, new Mine());
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
		if (field.get(nextFree) == null) {
			field.put(nextFree, parentBattle.getBot(botNumber));
		} else {
			if (!(field.get(nextFree) instanceof Mine)) {
				throw new IllegalStateException("Field to move to was neither free nor a mine.");
			}
			LOGGER.log(Level.FINE, "Bot " + botNumber + " stepped on a mine.");
			field.put(nextFree, null);
		}
	}

	private void fire(int botNumber, int targetPosition) {
		if (targetPosition >= 0 && targetPosition < fieldSize) {
			BattlefieldEntity firedAt = field.get(targetPosition);
			if (firedAt == null) {
				return;
			}
			field.remove(targetPosition);
			if (firedAt instanceof Bot && ((Bot) firedAt).getBotNumber() == botNumber) {
				LOGGER.log(Level.FINE, "Bot " + botNumber + " commited suicide.");
			} else if (firedAt instanceof Mine) {
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
			if (includeMined && field.get(i) instanceof Mine) {
				return i;
			}
		}
		return null;
	}

	private Integer getPosition(int botNumber) {
		for (Entry<Integer, BattlefieldEntity> entry : field.entrySet()) {
			if (entry.getValue() instanceof Bot && ((Bot) entry.getValue()).getBotNumber() == botNumber) {
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
			BattlefieldEntity entityOnField = field.get(i);
			if (entityOnField == null) {
				sb.append("-");
			} else if (entityOnField instanceof Mine) {
				sb.append("#");
			} else {
				sb.append(((Bot) entityOnField).getBotNumber());
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
		private final Map<Integer, BattlefieldEntity> _field;

		public BattlefieldView(int botNumber, int battlefieldSize, Map<Integer, BattlefieldEntity> field) {
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
			for (Entry<Integer, BattlefieldEntity> entry : _field.entrySet()) {
				if (entry.getValue() instanceof Bot
						&& ((Bot) entry.getValue()).getBotNumber() == moveResult.getBotNumber()) {
					return entry.getKey();
				}
			}
			return null;
		}

		public Integer whoIsAtPosition(int position) {
			BattlefieldEntity entity = _field.get(position);
			if (entity instanceof Bot) {
				return ((Bot) entity).getBotNumber();
			}
			return null;
		}

		@Override
		public String toString() {
			return "BattlefieldView [moveResult=" + moveResult + ", size=" + size + "]";
		}

	}

	public Integer getNumberOfMenStanding() {
		List<BattlefieldEntity> menStanding = field.values().stream()
				.filter(bot -> (bot != null && !(bot instanceof Mine))).collect(Collectors.toList());
		return menStanding.size();
	}

	public Bot getWinner() {
		List<Bot> menStanding = field.values().stream().filter(bot -> (bot != null && !(bot instanceof Mine)))
				.map(entity -> Bot.class.cast(entity)).collect(Collectors.toList());
		if (menStanding.size() != 1) {
			// LOGGER.log(Level.FINE, "No winner. Men standing are: " +
			// menStanding);
			return null;
		}
		return menStanding.get(0); // last man standing...
	}

	public boolean isBotAlive(int bot) {
		for (BattlefieldEntity field : this.field.values()) {
			if (field != null && field instanceof Bot && ((Bot) field).getBotNumber() == bot) {
				return true;
			}
		}
		return false;
	}

}
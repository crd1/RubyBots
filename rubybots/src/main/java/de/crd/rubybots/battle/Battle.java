package de.crd.rubybots.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import de.crd.rubybots.bots.BotConfig;
import de.crd.rubybots.config.Constants;
import de.crd.rubybots.engine.Engine;

public class Battle {
	private static final Logger LOGGER = Logger.getLogger(Battle.class.getSimpleName());
	private static final long TIME_BETWEEN_ROUNDS = 1000L;
	private final List<BotConfig> botConfigs;
	private final Integer numberOfRounds;
	private final UUID uuid = UUID.randomUUID();
	private final long startTime = System.currentTimeMillis();
	private final Battlefield battlefield;

	public Battle(Integer numberOfRounds, List<BotConfig> botConfigs) {
		this.numberOfRounds = numberOfRounds;
		this.botConfigs = Collections.unmodifiableList(botConfigs);
		this.battlefield = new Battlefield(this);
	}

	public Battle(List<BotConfig> botConfigs) {
		this(null, botConfigs);
	}

	public long getStartTime() {
		return startTime;
	}

	public int getNumberOfBots() {
		return botConfigs.size();
	}

	/**
	 * NON-API
	 */
	public void execute(Engine engine) {
		LOGGER.log(Level.FINE, "Begin of battle " + uuid);
		if (numberOfRounds != null) {
			executeRoundBasedBattle(engine);
		} else {
			executeLastManStanding(engine);
		}
		Integer winner = battlefield.getWinner();
		LOGGER.log(Level.FINE, "End of battle " + uuid + ". Winner is: " + ((winner != null) ? winner : "nobody"));
	}

	private void executeLastManStanding(Engine engine) {
		while (!battlefield.isOwned()) {
			callAllBots(battlefield.nextRound(), engine);
			sleepBetweenRounds();
		}
	}

	public BattleStats getCurrentBattleStats() {
		return this.battlefield.getBattleStats();
	}

	private void executeRoundBasedBattle(Engine engine) {
		do {
			if (battlefield.isOwned()) {
				LOGGER.log(Level.FINE,
						"Battle has already been won. Not executing round " + (battlefield.getCurrentRound() + 1));
				break;
			}
			battlefield.nextRound();
			callAllBots(battlefield.getCurrentRound(), engine);
			sleepBetweenRounds();
		} while (battlefield.getCurrentRound() < this.numberOfRounds);
	}

	private void sleepBetweenRounds() {
		try {
			Thread.sleep(TIME_BETWEEN_ROUNDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void callAllBots(int round, Engine engine) {
		LOGGER.log(Level.FINE, "--------------------------------------\nCalling all bots for round " + round);
		List<MoveResult> moveResults = new ArrayList<>();
		for (int currentBot = 0; currentBot < getNumberOfBots(); currentBot++) {
			if (!battlefield.isBotAlive(currentBot)) {
				LOGGER.log(Level.FINE, "Skipping bot " + currentBot + " since it has already been destroyed.");
				continue;
			}
			Context context = new Context(currentBot, round, battlefield.toView(currentBot), getNumberOfBots());
			try {
				engine.callBot(context);// this changes the battlefieldView
			} catch (ScriptException e) {
				LOGGER.log(Level.FINE, e.getMessage());
				LOGGER.log(Level.SEVERE, "Script of bot " + currentBot + " contained script error. Skipping bot.");
				continue;
			}
			MoveResult result = Battlefield.extractMoveResult(context.getBattlefield());
			moveResults.add(result);
			LOGGER.log(Level.FINE, "Obtained move result: " + result);
		}
		applyMoveResults(moveResults, engine);
		LOGGER.log(Level.FINE, "--------------------------------------\nEnd of round " + round);
	}

	private void applyMoveResults(List<MoveResult> moveResults, Engine engine) {
		List<Action> mergedActions = getMergedActionsStableShuffled(moveResults);
		Map<Integer, Integer> actionsPerBot = new HashMap<>();
		for (Action action : mergedActions) {
			Integer actionsOfThisBot = actionsPerBot.get(action.getBotNumber());
			if (actionsOfThisBot == null) {
				actionsOfThisBot = 0;
			}
			if (actionsOfThisBot >= Constants.MAX_ACTIONS_PER_BOT) {
				LOGGER.log(Level.FINE, "Skipping illegal action of bot " + action.getBotNumber());
				continue;
			}
			actionsPerBot.put(action.getBotNumber(), ++actionsOfThisBot);
			battlefield.applyAction(action);
			engine.getBattleStatsUpdateQueue().offer(battlefield.getBattleStats());
		}
	}

	static List<Action> getMergedActionsStableShuffled(List<MoveResult> moveResults) {
		Random random = new Random();
		List<Action> mergedActions = new ArrayList<>();
		List<List<Action>> actionSets = moveResults.stream().map(result -> result.getActions())
				.filter(list -> list != null && !list.isEmpty()).collect(Collectors.toList());
		while (!actionSets.isEmpty()) {
			int takeFrom = random.nextInt(actionSets.size());
			mergedActions.add(actionSets.get(takeFrom).get(0));
			actionSets.get(takeFrom).remove(0);
			if (actionSets.get(takeFrom).isEmpty()) {
				actionSets.remove(takeFrom);
			}
		}
		return mergedActions;
	}

	public List<BotConfig> getBotConfigs() {
		return botConfigs;
	}
}

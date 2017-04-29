package de.crd.rubybots.battle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import de.crd.rubybots.engine.Engine;

public class Battle {
	private static final Integer MAX_ACTIONS_PER_BOT = 3;
	private final int numberOfBots;
	private final Integer numberOfRounds;
	private final UUID uuid = UUID.randomUUID();
	private final long startTime = System.currentTimeMillis();
	private final Battlefield battlefield;

	public Battle(int numberOfBots, int numberOfRounds) {
		this.numberOfBots = numberOfBots;
		this.numberOfRounds = numberOfRounds;
		this.battlefield = new Battlefield(this);
	}

	public Battle(int numberOfBots) {
		this.numberOfBots = numberOfBots;
		this.numberOfRounds = null;
		this.battlefield = new Battlefield(this);
	}

	public long getStartTime() {
		return startTime;
	}

	public int getNumberOfBots() {
		return numberOfBots;
	}

	public void execute() {
		System.out.println("Begin of battle " + uuid);
		if (numberOfRounds != null) {
			executeRoundBasedBattle();
		} else {
			executeLastManStanding();
		}
		Integer winner = battlefield.getWinner();
		System.out.println("End of battle " + uuid + ". Winner is: " + ((winner != null) ? winner : "nobody"));
	}

	private void executeLastManStanding() {
		while (!battlefield.isOwned()) {
			callAllBots(battlefield.nextRound());
		}
	}

	public BattleStats getCurrentBattleStats() {
		return this.battlefield.getBattleStats();
	}

	private void executeRoundBasedBattle() {
		do {
			battlefield.nextRound();
			if (battlefield.isOwned()) {
				System.out.println("Battle has already been won. Not executing round " + battlefield.getCurrentRound());
				break;
			}
			callAllBots(battlefield.getCurrentRound());
		} while (battlefield.getCurrentRound() < this.numberOfRounds);
	}

	private void callAllBots(int round) {
		System.out.println("--------------------------------------\nCalling all bots for round " + round);
		List<MoveResult> moveResults = new ArrayList<>();
		for (int currentBot = 0; currentBot < numberOfBots; currentBot++) {
			Context context = new Context(currentBot, round, battlefield.toView(currentBot), numberOfBots);
			try {
				Engine.callBot(context);// this changes the battlefieldView
			} catch (ScriptException e) {
				System.out.println(e.getMessage());
				System.out.println("Script of bot " + currentBot + " contained script error. Skipping bot.");
				continue;
			}
			MoveResult result = Battlefield.extractMoveResult(context.getBattlefield());
			moveResults.add(result);
			System.out.println("Obtained move result: " + result);
		}
		applyMoveResults(moveResults);
		System.out.println("--------------------------------------\nEnd of round " + round);
	}

	private void applyMoveResults(List<MoveResult> moveResults) {
		List<Action> mergedActions = getMergedActionsStableShuffled(moveResults);
		Map<Integer, Integer> actionsPerBot = new HashMap<>();
		for (Action action : mergedActions) {
			Integer actionsOfThisBot = actionsPerBot.get(action.getBotNumber());
			if (actionsOfThisBot == null) {
				actionsOfThisBot = 0;
			}
			if (actionsOfThisBot >= MAX_ACTIONS_PER_BOT) {
				System.out.println("Skipping illegal action of bot " + action.getBotNumber());
				continue;
			}
			actionsPerBot.put(action.getBotNumber(), ++actionsOfThisBot);
			battlefield.applyAction(action);
			Engine.getBattleStatsUpdateQueue().offer(battlefield.getBattleStats());
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
}

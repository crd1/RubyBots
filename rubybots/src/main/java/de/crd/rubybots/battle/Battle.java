package de.crd.rubybots.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import de.crd.rubybots.engine.Engine;

public class Battle {
	private static final Integer MAX_ACTIONS_PER_BOT = 3;
	private final int numberOfBots = Engine.getNumberOfBots();
	private final Integer numberOfRounds;
	private final UUID uuid = UUID.randomUUID();
	private final long startTime = System.currentTimeMillis();
	private final Battlefield battlefield = new Battlefield(this);

	public Battle(int numberOfRounds) {
		this.numberOfRounds = numberOfRounds;
	}

	public Battle() {
		this.numberOfRounds = null;
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
		List<Action> mergedActions = moveResults.stream().map(result -> result.getActions())
				.flatMap(actions -> actions.stream()).collect(Collectors.toList());
		Collections.shuffle(mergedActions);
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
}

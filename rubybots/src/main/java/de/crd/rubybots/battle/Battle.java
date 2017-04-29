package de.crd.rubybots.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import de.crd.rubybots.engine.Engine;

public class Battle {
	private static final Integer MAX_ACTIONS_PER_BOT = 3;
	private final int numberOfBots = Engine.getNumberOfBots();
	private final Integer numberOfRounds;
	private final UUID uuid = UUID.randomUUID();
	private final Battlefield battlefield = new Battlefield();

	public Battle(int numberOfRounds) {
		this.numberOfRounds = numberOfRounds;
	}

	public Battle(Queue<BattleStats> statsUpdateQueue) {
		this.numberOfRounds = null;
	}

	public void execute() {
		System.out.println("Begin of battle " + uuid);
		if (numberOfRounds != null) {
			executeRoundBasedBattle();
		} else {
			executeLastManStanding();
		}
		System.out.println("End of battle " + uuid);
	}

	private void executeLastManStanding() {
		int currentRound = 1;
		while (!battlefield.isOwned()) {
			callAllBots(currentRound++);
		}
	}

	private void executeRoundBasedBattle() {
		for (int currentRound = 1; currentRound <= this.numberOfRounds; currentRound++) {
			callAllBots(currentRound);
		}
	}

	private void callAllBots(int round) {
		System.out.println("--------------------------------------\nCalling all bots for round " + round);
		List<MoveResult> moveResults = new ArrayList<>();
		for (int currentBot = 0; currentBot < numberOfBots; currentBot++) {
			Context context = new Context(currentBot, round, battlefield.toView(), numberOfBots);
			Engine.callBot(context); // this changes the battlefieldView
			MoveResult result = Battlefield.extractMoveResult(context.getBattlefield());
			result.setBotNumber(currentBot);
			moveResults.add(result);
			System.out.println("Obtained move result: " + result);
		}
		applyMoveResults(moveResults);
		System.out.println("--------------------------------------\nEnd of round " + round);
	}

	private void applyMoveResults(List<MoveResult> moveResults) {
		Collections.shuffle(moveResults);
		Map<Integer, Integer> actionsPerBot = new HashMap<>();
		for (MoveResult result : moveResults) {
			Integer actionsOfThisBot = actionsPerBot.get(result.getBotNumber());
			if (actionsOfThisBot == null) {
				actionsOfThisBot = 0;
			}
			if (actionsOfThisBot > MAX_ACTIONS_PER_BOT) {
				System.out.println("Skipping illegal action of bot " + result.getBotNumber());
				continue;
			}
			actionsPerBot.put(result.getBotNumber(), ++actionsOfThisBot);
			battlefield.applyMoveResult(result);
			Engine.getBattleStatsUpdateQueue().offer(battlefield.getBattleStats());
		}
	}
}

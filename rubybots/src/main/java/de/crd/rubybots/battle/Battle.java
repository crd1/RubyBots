package de.crd.rubybots.battle;

import java.util.Queue;
import java.util.UUID;

import de.crd.rubybots.engine.Engine;

public class Battle {
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
		for (int currentBot = 0; currentBot < numberOfBots; currentBot++) {
			Context context = new Context(currentBot, round, battlefield.immutableCopy(), numberOfBots);
			MoveResult result = Engine.callBot(context);
			result.setBotNumber(currentBot);
			battlefield.applyMoveResult(result);
			Engine.getBattleStatsUpdateQueue().offer(battlefield.getBattleStats());
			System.out.println("Obtained move result: " + result);
		}
		System.out.println("--------------------------------------\nEnd of round " + round);
	}
}

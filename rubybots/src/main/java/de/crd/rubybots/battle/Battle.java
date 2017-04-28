package de.crd.rubybots.battle;

import de.crd.rubybots.Engine;

public class Battle {
	private final int numberOfBots = Engine.getNumberOfBots();
	private final int numberOfRounds;

	public Battle(int numberOfRounds) {
		this.numberOfRounds = numberOfRounds;
	}

	public void execute() {
		for (int i = 0; i < this.numberOfRounds; i++) {
			callAllBots(i);
		}
	}

	private void callAllBots(int round) {
		System.out.println("--------------------------------------\nCalling all bots for round " + round);
		for (int i = 0; i < numberOfBots; i++) {
			Context context = new Context(i, round);
			MoveResult result = Engine.callBot(context);
			System.out.println("Move result for bot " + i + " is " + result);
		}
		System.out.println("--------------------------------------\nEnd of round " + round);
	}
}

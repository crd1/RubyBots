package de.crd.rubybots.battle;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.crd.rubybots.battle.Action.ActionType;

public class BattleStats {

	private final long timestamp;
	private final Battlefield battleField;
	private final Integer winner;
	private final int rounds;
	private final int numberOfBots;

	public BattleStats(long battleStart, int numberOfBots, Battlefield battleField) {
		this.numberOfBots = numberOfBots;
		this.battleField = battleField;
		this.rounds = battleField.getCurrentRound();
		this.winner = battleField.getWinner();
		this.timestamp = System.currentTimeMillis() - battleStart;
	}

	@Override
	public String toString() {
		return "BattleStats [timestamp=" + timestamp + ", battleField=" + battleField + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BattleStats other = (BattleStats) obj;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}

	public Integer getWinner() {
		return winner;
	}

	public int getRounds() {
		return rounds;
	}

	public Battlefield getBattlefield() {
		return battleField;
	}

	public static BattleStats calculateStats(long startTime, int numberOfBots, Battlefield battlefield) {
		// TODO further values??
		return new BattleStats(startTime, numberOfBots, new Battlefield(battlefield));
	}

	public Map<ActionType, Integer> getSummedUpHistory() {
		Map<ActionType, Integer> result = new HashMap<>();
		for (ActionType actionType : ActionType.values()) {
			result.put(actionType, 0);
		}
		for (Entry<Integer, Map<ActionType, Integer>> allHistory : battleField.getHistory().entrySet()) {
			for (Entry<ActionType, Integer> singleBotHistoryEntry : allHistory.getValue().entrySet()) {
				result.put(singleBotHistoryEntry.getKey(),
						result.get(singleBotHistoryEntry.getKey()) + singleBotHistoryEntry.getValue());
			}
		}
		return result;
	}

	public Map<Integer, Map<ActionType, Integer>> getHistory() {
		return battleField.getHistory();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getNumberOfBots() {
		return numberOfBots;
	}

}

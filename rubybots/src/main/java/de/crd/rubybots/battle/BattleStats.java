package de.crd.rubybots.battle;

public class BattleStats {

	private final long timestamp;
	private final Battlefield battleField;

	public BattleStats(long battleStart, Battlefield battleField) {
		this.battleField = battleField;
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

	public static BattleStats calculateStats(long startTime, Battlefield battlefield) {
		// TODO
		return new BattleStats(startTime, new Battlefield(battlefield));
	}

}

package de.crd.rubybots.battle;

public class BattleStats {

	private final long timestamp = System.currentTimeMillis();

	@Override
	public String toString() {
		return "BattleStats [timestamp=" + timestamp + "]";
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

}

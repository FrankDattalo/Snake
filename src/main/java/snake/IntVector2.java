package snake;

public class IntVector2 {
	private final int x;
	private final int y;
	
	public IntVector2(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() { return this.x; }
	
	public int getY() { return this.y; }
	
	public IntVector2 add(IntVector2 other) {
		return new IntVector2(x + other.x, y + other.y);
	}
	
	public boolean hasMagnitudeZero() {
		return this.x == 0 && this.y == 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IntVector2))
			return false;
		IntVector2 other = (IntVector2) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IntVector2 [x=" + x + ", y=" + y + "]";
	}
}

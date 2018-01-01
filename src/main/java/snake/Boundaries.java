package snake;

public class Boundaries {
	
	private final int minX;
	private final int maxX;
	private final int minY;
	private final int maxY;
	
	public Boundaries(int minX, int maxX, int minY, int maxY) {
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
	}
	
	public boolean isInBounds(IntVector2 vector) {
		return minX <= vector.getX() && vector.getX() < maxX &&
			   minY <= vector.getY() && vector.getY() < maxY;
	}
	
	public int getMinX() {
		return minX;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMinY() {
		return minY;
	}

	public int getMaxY() {
		return maxY;
	}

	@Override
	public String toString() {
		return "Boundaries [minX=" + minX + ", maxX=" + maxX + ", minY=" + minY + ", maxY=" + maxY + "]";
	}

}

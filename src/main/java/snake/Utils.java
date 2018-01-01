package snake;

import java.util.Random;

public class Utils {
	private static final Random random = new Random();
	
	private static int randomIntInRange(int lowInclusive, int highExclusive) {
		return random.nextInt(highExclusive - lowInclusive) + lowInclusive;
	}
	
	public static IntVector2 randomVectorInBounds(Boundaries boundaries) {
		return new IntVector2(Utils.randomIntInRange(boundaries.getMinX(), boundaries.getMaxX()), 
							  Utils.randomIntInRange(boundaries.getMinY(), boundaries.getMaxY()));
	}

	public static IntVector2 randomDirection() {
		switch(random.nextInt(4)) {
		case 0:  return new IntVector2(1, 0); // left
		case 1:  return new IntVector2(-1, 0); // right
		case 2:  return new IntVector2(0, 1); // down
		// case 3
		default: return new IntVector2(0, -1); // up
		}
	}
}

package snake;

public interface Controller {
	
	public static final IntVector2 UP    = new IntVector2(0, -1);
	public static final IntVector2 DOWN  = new IntVector2(0, 1);
	public static final IntVector2 LEFT  = new IntVector2(-1, 0);
	public static final IntVector2 RIGHT = new IntVector2(1, 0);
	
	public Player getPlayer();
	
	public IntVector2 getDirection();
}

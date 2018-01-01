package snake;

import java.util.ArrayList;
import java.util.Collection;

public class Snake {

	private ArrayList<IntVector2> segments = new ArrayList<IntVector2>();
	private int amountToGrow = 0;
	
	public Snake(IntVector2 head) {
		segments.add(head);
	}
	
	public IntVector2 head() {
		return this.segments.get(0);
	}
	
	public void move(IntVector2 direction) {
		IntVector2 tail = segments.get(segments.size() - 1);
		IntVector2 head = segments.get(0);
		
		for (int i = segments.size() - 1; i > 0; i--) {
			segments.set(i, segments.get(i - 1));
		}
		
		segments.set(0, head.add(direction));
		
		if (amountToGrow > 0) {
			segments.add(tail);
			amountToGrow--;
		}
	}
	
	public int length() {
		return segments.size();
	}
	
	public Collection<IntVector2> segments() {
		return this.segments;
	}
	
	public void growOnNextMove() {
		amountToGrow++;
	}
}

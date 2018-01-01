package snake;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class Player {
	
	private Snake snake;
	private IntVector2 movementDirection;
	private long lastUpdateTime;
	private boolean dead = false;
	private int score = 0;
	
	private final String name;
	private final Color color;
	private final Set<IntVector2> foodLocations;
	private final Boundaries boundaries;
	private final List<IntVector2> snakeSegments;
	
	private final ReadWriteLock dirLock = new ReentrantReadWriteLock();
	
	public Player(Boundaries boundaries, Set<IntVector2> foodLocations, List<IntVector2> snakeSegments, String name, Color color) {
		this.boundaries = boundaries;
		this.foodLocations = foodLocations;
		this.snakeSegments = snakeSegments;
		this.name = name;
		this.color = color;
		this.snake = new Snake(Utils.randomVectorInBounds(new Boundaries(boundaries.getMinX() + 3, 
																		 boundaries.getMaxX() - 3, 
																		 boundaries.getMinY() + 3, 
																		 boundaries.getMaxY() - 3)));
		this.movementDirection = Utils.randomDirection();
	}
	
	public void setMovementDirection(IntVector2 dir) {
		try {
			this.dirLock.writeLock().lock();
			
			if (dir == null) return;
			
			// snakes of length one can move in any direction
			if (snake.length() == 1) {
				this.movementDirection = dir;
				return;
			}
			
			if (movementDirection.add(dir).hasMagnitudeZero()) return;
		
			this.movementDirection = dir;
		} finally {
			this.dirLock.writeLock().unlock();
		}
	}
	
	private long timeBetweenUpdates() {
		return Math.max(10, 150 - snake.length() + 1);
	}
	
	public void update() {
		if (dead) return;
		
		// sleep logic
		long end = System.currentTimeMillis();
		long secondsSinceLastUpdate = end - this.lastUpdateTime;
		if (secondsSinceLastUpdate < timeBetweenUpdates()) return;
		this.lastUpdateTime = end;
		
		// boundaries logic
		if (!boundaries.isInBounds(snake.head())) {
			dead = true;
			return;
		}
		
		// eating self or other snake logic
		IntVector2 snakeHead = this.snake.head();
		this.snakeSegments.remove(snakeHead);
		if (this.snakeSegments.contains(snakeHead)) {
			dead = true;
		}
		this.snakeSegments.add(snakeHead);
		if (dead) return;
		
		// movement logic
		this.snakeSegments.removeAll(this.snake.segments());
		try {
			this.dirLock.readLock().lock();
			this.snake.move(movementDirection);
		} finally {
			this.dirLock.readLock().unlock();
		}
		
		this.snakeSegments.addAll(this.snake.segments());
		
		// grow logic
		for (IntVector2 segment : this.snake.segments()) {
			if (this.foodLocations.contains(segment)) {
				// score logic
				score += this.snake.segments().size() * 2;
				this.snake.growOnNextMove();
				this.foodLocations.remove(segment);
			}
		}
	}
	
	public boolean alive() {
		return !dead;
	}
	
	public void forEachSnakeSegment(Consumer<IntVector2> consumer) {
		this.snake.segments().forEach(consumer);
	}
	
	public IntVector2 getSnakeHead() {
		return this.snake.head();
	}

	public String getName() {
		return name;
	}

	public Color getColor() {
		return color;
	}
	
	public int getScore() {
		return this.score;
	}
}

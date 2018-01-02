package snake;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class Player {
	
	private Snake snake;
	private IntVector2 movementDirection;
	private IntVector2 previousMovementDirection;
	private long lastUpdateTime;
	private boolean dead = false;
	private int score = 0;
	private boolean tron;
	private final long passiveScoreTime = 1000; // 1 second
	private long lastScoreUpdate;

	private final Game game;
	private final String name;
	private final Color color;
	private final Set<IntVector2> foodLocations;
	private final Boundaries boundaries;
	private final Map<IntVector2, Player> snakeSegments;
	
	private final ReadWriteLock dirLock = new ReentrantReadWriteLock();
	
	public Player(Boundaries boundaries, Set<IntVector2> foodLocations, 
				  Map<IntVector2, Player> snakeSegments, String name, Color color, boolean tron, Game game) {
		this.boundaries = boundaries;
		this.foodLocations = foodLocations;
		this.snakeSegments = snakeSegments;
		this.name = name;
		this.color = color;
		this.game = game;
		this.reset(tron);
	}

	public void reset(boolean tron) {
		this.snake = new Snake(Utils.randomVectorInBounds(new Boundaries(boundaries.getMinX() + 3, 
																		 boundaries.getMaxX() - 3, 
																		 boundaries.getMinY() + 3, 
																		 boundaries.getMaxY() - 3)));
		this.movementDirection = Utils.randomDirection();
		this.previousMovementDirection = this.movementDirection;
		this.score = 0;
		this.dead = false;
		this.snakeSegments.put(this.snake.head(), this);
		this.tron = tron;
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
			
			if (this.previousMovementDirection.add(dir).hasMagnitudeZero()) return;
		
			this.movementDirection = dir;
		} finally {
			this.dirLock.writeLock().unlock();
		}
	}
	
	private long timeBetweenUpdates() {
		try {
			this.dirLock.readLock().lock();
			long base = Math.max(100, 150 - snake.length() + 1);
			if (this.movementDirection.equals(Controller.UP) || this.movementDirection.equals(Controller.DOWN)) {
				base *= 1.25;
			}
			return base;
		} finally {
			this.dirLock.readLock().unlock();
		}
	}

	private void passiveScoreUpdate() {
		long end = System.currentTimeMillis();
		long delta = end - this.lastScoreUpdate;
		if (delta < this.passiveScoreTime) return;
		this.lastScoreUpdate = end;
		this.score += Math.max(snake.segments().size() / 8, 1);
	}

	private void survivorPoints(Player other) {
		game.forEachPlayer(player -> {
			if (player == this || player == other || player.dead) return;
			player.score += this.score / 2;
		});
	}
	
	public void update() {
		if (dead) return;

		passiveScoreUpdate();
		
		// sleep logic
		long end = System.currentTimeMillis();
		long secondsSinceLastUpdate = end - this.lastUpdateTime;
		if (secondsSinceLastUpdate < timeBetweenUpdates()) return;
		this.lastUpdateTime = end;
		
		try {
			this.dirLock.readLock().lock();
		
			IntVector2 nextHead = snake.head().add(this.movementDirection);

			// boundaries logic
			if (!boundaries.isInBounds(nextHead)) {
				dead = true;
				this.survivorPoints(null);
				return;
			}
		
			// eating self or other snake logic
			if (this.snakeSegments.containsKey(nextHead)) {
				Player other = this.snakeSegments.get(nextHead);
				if (other != this) {
					other.score += this.score;
				}
				dead = true;
				this.survivorPoints(other);
				return;
			}
			
			// movement logic
			this.snake.segments().forEach(this.snakeSegments::remove);
			this.snake.move(movementDirection);
			this.previousMovementDirection = this.movementDirection;
			this.snake.segments().forEach(segment -> this.snakeSegments.put(segment, this));
			
		} finally {
			this.dirLock.readLock().unlock();
		}

		if (tron) {
			this.snake.growOnNextMove();
			return;
		}
		
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

	public String getScoreDescription() {
		return String.format("%-10s %10d", getName(), getScore());
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

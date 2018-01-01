package snake;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class Game {
	
	private final ReadWriteLock objectLock = new ReentrantReadWriteLock();
	
	private final List<Player> players = new ArrayList<Player>();
	private final List<IntVector2> snakeSegments = new ArrayList<IntVector2>();
	private final Set<IntVector2> foodSpots = new HashSet<IntVector2>();
	private final Boundaries boundaries;
	
	private final long timeBetweenFood = 2000; // 2 seconds
	private long lastFoodSpawn;
	
	private static final Color[] COLORS = new Color[] {Color.Yellow, Color.Magenta, Color.Cyan, Color.White};
	
	public Game(int rows, int cols) {
		this.boundaries = new Boundaries(1, cols - 1, 1, rows - 1);
	}
	
	public int playerCount() {
		try {
			this.objectLock.readLock().lock();
			return this.players.size();
		} finally {
			this.objectLock.readLock().unlock();
		}
	}
	
	public Player addPlayer(String name) {		
		try {
			this.objectLock.writeLock().lock();
			
			if (players.size() > 3) return null;
			if (name == null) return null;
			if (name.isEmpty()) return null;
			if (players.stream().anyMatch(player -> 
				player.getName().equalsIgnoreCase(name))) return null;
			
			Color color = COLORS[players.size()];
			Player player = new Player(boundaries, foodSpots, snakeSegments, name, color);
			players.add(player);
			this.snakeSegments.add(player.getSnakeHead());
			return player;
			
		} finally {
			this.objectLock.writeLock().unlock();
		}
	}
	
	private int numAlivePlayers() {
		try {
			this.objectLock.readLock().lock();
			return (int) this.players.stream().filter(Player::alive).count();
		} finally {
			this.objectLock.readLock().unlock();
		}
	}
	
	private void foodUpdate() {
		long end = System.currentTimeMillis();
		if (end - lastFoodSpawn < timeBetweenFood) return;
		lastFoodSpawn = end;
		
		try {
			this.objectLock.writeLock().lock();
			this.foodSpots.add(Utils.randomVectorInBounds(boundaries));
		} finally {
			this.objectLock.writeLock().unlock();
		}
	}
	
	public void start() {
		
		while (true) {
			int numPlayersAlive = this.numAlivePlayers();
			int numPlayers = this.players.size();
			
			// case 1 - there is only one alive player and it is a multi-player game -> return winningPlayer()
			// case 2 - there is only one alive player and it is a single player game -> continue
			// case 3 - there are no players alive and it is a multi-player game -> return null (Tie)
			// case 4 - there is no players alive and it is a single player game -> return player
			if (numPlayersAlive == 1 && numPlayers > 1) return;
			if (numPlayersAlive == 1 && numPlayers == 1);
			if (numPlayersAlive == 0 && numPlayers > 1) return;
			if (numPlayersAlive == 0 && numPlayers == 1) return;
			
			foodUpdate();
			
			try {
				this.objectLock.writeLock().lock();
				players.forEach(Player::update);
			} finally {
				this.objectLock.writeLock().unlock();
			}
		}
	}
	
	public List<Player> getPlayers() {
		return this.players;
	}
	
	public Boundaries getBoundaries() {
		return this.boundaries;
	}
	
	public void forEachPlayer(Consumer<Player> consumer) {
		try {
			this.objectLock.readLock().lock();
			this.players.forEach(consumer);
		} finally {
			this.objectLock.readLock().unlock();
		}
	}
	
	public void forEachFoodSpot(Consumer<IntVector2> consumer) {
		try {
			this.objectLock.readLock().lock();
			this.foodSpots.forEach(consumer);	
		} finally {
			this.objectLock.readLock().unlock();
		}
	}
}

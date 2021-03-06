package snake;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration;

public class TerminalGameDisplayer implements Runnable {

	private long lastUpdate;
	private Terminal terminal;
	
	private final long millisBetweenUpdates = 68; // 60 fps = 17, 30 fps = 34, 15 fps = 68
	private final Game game;
	private final int rows;
	private final int cols;
	private final Thread mainThread;
	
	public TerminalGameDisplayer(Game game, Thread main) {
		this.rows = game.getBoundaries().getMaxY() + 1;
		this.cols = game.getBoundaries().getMaxX() + 1;
		this.game = game;
		this.mainThread = main;
	}
	
	private boolean shouldSleep() {
		long end = System.currentTimeMillis();
		long delta = end - lastUpdate;
		if (delta < millisBetweenUpdates) return true;
		
		lastUpdate = end;
		
		return false;
	}
	
	private void writeString(Terminal terminal, String str, int x, int y, TextColor color) {
		try {
			terminal.setForegroundColor(color);
			terminal.setCursorPosition(x, y);
			char[] chars = str.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				terminal.putCharacter(chars[i]);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void drawLoop(Terminal terminal, Game game) throws IOException, InterruptedException {
		terminal.setCursorVisible(false);
		terminal.enableSGR(SGR.BOLD);
		terminal.setBackgroundColor(TextColor.ANSI.BLACK);
		
		Boundaries boundaries = game.getBoundaries();
		
		while (this.mainThread.isAlive()) {			
			if (shouldSleep()) continue;
			
			terminal.clearScreen();
			
			// food drawing
			game.forEachFoodSpot(food -> writeString(terminal, "#", food.getX(), food.getY(), TextColor.ANSI.WHITE));
			
			// horizontal boundaries drawing
			for (int x = boundaries.getMinX(); x < boundaries.getMaxX(); x++) {
				writeString(terminal, "-", x, 0,                    TextColor.ANSI.WHITE);
				writeString(terminal, "-", x, boundaries.getMaxY(), TextColor.ANSI.WHITE);
			}
			
			// vertical boundaries drawing
			for (int y = boundaries.getMinY(); y < boundaries.getMaxY(); y++) {
				writeString(terminal, "|", 0,                    y, TextColor.ANSI.WHITE);
				writeString(terminal, "|", boundaries.getMaxX(), y, TextColor.ANSI.WHITE);
			}
			
			// corner boundaries drawing
			writeString(terminal, "+", 0,                    0,                    TextColor.ANSI.WHITE);
			writeString(terminal, "+", boundaries.getMaxX(), 0,                    TextColor.ANSI.WHITE);
			writeString(terminal, "+", boundaries.getMaxX(), boundaries.getMaxY(), TextColor.ANSI.WHITE);
			writeString(terminal, "+", 0,                    boundaries.getMaxY(), TextColor.ANSI.WHITE);

			// score displaying
			AtomicInteger counter = new AtomicInteger(0);
						
			game.forEachPlayer(player -> { 
				TextColor color = null;
				switch (player.getColor()) {
					case White: color = TextColor.ANSI.WHITE; break;
					case Magenta: color = TextColor.ANSI.MAGENTA; break;
					case Cyan: color = TextColor.ANSI.CYAN; break;
					default: 
					case Yellow: color = TextColor.ANSI.YELLOW; break;
				}

				int x;
				int y;
				String score = String.valueOf(player.getScore());
				switch (counter.getAndIncrement()) {
					case 0:
						// top left corner
						x = 1;
						y = 0;
						break;
					case 1:
						// top right corner
						x = this.game.getBoundaries().getMaxX() - score.length();
						y = 0;
						break;
					case 2:
						// bottom left corner
						x = 1;
						y = this.game.getBoundaries().getMaxY();
						break;
					default:
					case 3:
						// bottom right corner
						x = this.game.getBoundaries().getMaxX() - score.length();
						y = this.game.getBoundaries().getMaxY();
				}

				writeString(terminal, score, x, y, color);

				// java is stupid
				final TextColor segmentColor = color;
				
				player.forEachSnakeSegment(segment -> 
					writeString(terminal, "O", segment.getX(), segment.getY(), segmentColor));
			});
			
			if (game.isGameOver()) {
				List<Player> scores = 
					game.getPlayers()
						.stream()
						.sorted((a, b) -> b.getScore() - a.getScore())
						.collect(Collectors.toList());
		
				String clearString = "                           ";

				writeString(terminal, clearString, 1, 1, TextColor.ANSI.WHITE);
				writeString(terminal, clearString, 1, 2, TextColor.ANSI.WHITE);
				writeString(terminal, "Scores:", 2, 2, TextColor.ANSI.WHITE);
				for (int i = 0, y = 3; i < scores.size(); i++, y++) {
					TextColor color = null;
					switch (scores.get(i).getColor()) {
						case White: color = TextColor.ANSI.WHITE; break;
						case Magenta: color = TextColor.ANSI.MAGENTA; break;
						case Cyan: color = TextColor.ANSI.CYAN; break;
						default: 
						case Yellow: color = TextColor.ANSI.YELLOW; break;
					}
					writeString(terminal, clearString, 1, y, TextColor.ANSI.WHITE);
					writeString(terminal, clearString, 1, y + 1, TextColor.ANSI.WHITE);
					writeString(terminal, scores.get(i).getScoreDescription(), 2, y, color);
				}
			}

			terminal.flush();
		}
	}
	
	@Override
	public void run() {		
		try {
			DefaultTerminalFactory factory = new DefaultTerminalFactory(System.out, System.in, Charset.forName("UTF8"));
			
			factory.setTerminalEmulatorTitle("Snake");
			factory.setInitialTerminalSize(new TerminalSize(cols, rows));
			factory.setTerminalEmulatorFontConfiguration(
				AWTTerminalFontConfiguration.getDefault());

			// using reflection to get at font size variable
			Field field = AWTTerminalFontConfiguration.class.getDeclaredField("CHOSEN_FONT_SIZE");
			field.setAccessible(true);
			field.set(null, Integer.valueOf(56)); // magic number, the value of the font size that seems to work well

			terminal = factory.createTerminalEmulator();
			terminal.enterPrivateMode();
			drawLoop(terminal, game);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				terminal.exitPrivateMode();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}

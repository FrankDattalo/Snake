package snake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

public class Main {
	
	private static String prompt(String str, BufferedReader reader) {
		System.out.print(str);
		try {
			return reader.readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) {
		boolean skipNames = args.length > 0 && "--skip-names".equalsIgnoreCase(args[0]);

		ControllerManager controllerManager = new ControllerManager();
		
		try {
			controllerManager.initSDLGamepad();
		
			int rows = 20;
			int cols = 80;
			
			Game game = new Game(rows, cols);
			TerminalGameDisplayer displayer = new TerminalGameDisplayer(game, Thread.currentThread());
			ControllerRunner controllers = new ControllerRunner(Thread.currentThread());
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			
			int numControllers = controllerManager.getNumControllers();
			
			System.out.println(numControllers + " controllers detected.");
			
			if (numControllers < 1) return;
			
			int numPlayers = skipNames ? numControllers : Integer.parseInt(prompt("Enter number of players> ", reader));
		
			if (numPlayers < 1 || numPlayers > numControllers) {
				System.out.println("Invalid number of players.");
				return;
			}
			
			for (int i = 0; i < numPlayers && i < numControllers; i++) {
				String name = skipNames ? "Player " + (i + 1) : prompt("Enter Player " + (i + 1) + "'s name> ", reader);
				Player player = game.addPlayer(name);
				Controller controller = new JpadController(player, i, controllerManager);
				controllers.addController(controller);
			}
			
			new Thread(displayer).start();
			
			Thread controllerThread = new Thread(controllers);
			controllerThread.start();
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			game.start();
			
			game.getPlayers().stream()
			    .sorted((a, b) -> b.getScore() - a.getScore())
			    .forEach(player ->
					System.out.printf("%-8s %-10s %10d%s", 
						"(" + player.getColor() + ")", player.getName(), 
						player.getScore(), System.lineSeparator()));
			
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			controllers.stop();
			
			try {
				controllerThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		} finally {
			controllerManager.quitSDLGamepad();
		}
	}
	
	private static class JpadController implements Controller {

		private ControllerManager manager;
		private int index;
		private Player player;

		public JpadController(Player player, int controllerIndex, ControllerManager manager) {
			this.player = player;
			this.index = controllerIndex;
			this.manager = manager;
		}
		
		@Override
		public Player getPlayer() {
			return player;
		}

		@Override
		public IntVector2 getDirection() {
			ControllerState state = manager.getState(index);
			
			if (state.dpadDown) return Controller.DOWN;
			if (state.dpadRight) return Controller.RIGHT;
			if (state.dpadLeft) return Controller.LEFT;
			if (state.dpadUp) return Controller.UP;
			
			return null;
		}		
	}
}
package snake;

import java.util.ArrayList;

public class ControllerRunner implements Runnable {

	private ArrayList<Controller> controllers = new ArrayList<Controller>();
	private Thread mainThread;
	private boolean stop;
	private Game game;

	public ControllerRunner(Game game, Thread main) {
		this.mainThread = main;
		this.game = game;
	}
	
	public void addController(Controller controller) {
		this.controllers.add(controller);
	}
	
	public void stop() {
		stop = true;
	}
	
	@Override
	public void run() {
		while (!stop && this.mainThread.isAlive()) {
			for (Controller controller : controllers) {
				game.reset(controller.getGameReset());
				game.quit(controller.getGameQuit());
				controller.getPlayer().setMovementDirection(controller.getDirection());
			}
		}
	}
}

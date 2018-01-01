package snake;

import java.util.ArrayList;

public class ControllerRunner implements Runnable {

	private ArrayList<Controller> controllers = new ArrayList<Controller>();
	private Thread mainThread;
	private boolean stop;
	
	public ControllerRunner(Thread main) {
		this.mainThread = main;
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
				controller.getPlayer().setMovementDirection(controller.getDirection());
			}
		}
	}
}

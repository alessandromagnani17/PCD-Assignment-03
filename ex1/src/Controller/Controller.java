package main.java.ass03_parte1.Controller;

import akka.actor.typed.ActorSystem;
import main.java.ass03_parte1.Model.*;
import main.java.ass03_parte1.View.SimulationView;

import java.util.ArrayList;

/**
 * Represents the Controller for the GUI.
 *
 */
public class Controller {

	private final SimulationView viewer;
	private ActorSystem<WorkerProtocol> masterActor;
	private final int nBalls;
	private final int nSteps;
	private final int nWorkers;

	/**
	 * Creates a new Controller specifying the SimulationView.
	 *
	 * @param viewer The SimulationView.
	 */
	public Controller(SimulationView viewer, int nSteps, int nBalls, int nWorkers) {
		this.viewer = viewer;
		this.nSteps = nSteps;
		this.nBalls = nBalls;
		this.nWorkers = nWorkers;
	}

	public void display(ArrayList<Body> bodies, double vt, int actualSteps, Boundary bounds) {
		this.viewer.display(bodies, vt, actualSteps, bounds);
	}

	/**
	 * Start the simulation creating balls and starting the Master Agent.
	 *
	 */
	public void notifyStarted() {
		this.masterActor = ActorSystem.create(MasterActor.create(this, true, this.nSteps, this.nBalls, this.nWorkers), "MasterActor");
		this.masterActor.tell(new WorkerProtocol.BootMsg());
	}

	/**
	 * Stop the simulation.
	 *
	 */
	public void notifyStopped() {
		this.masterActor.tell(new WorkerProtocol.StopMsg());
	}

}

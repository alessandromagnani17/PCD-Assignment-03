package main.java.ass03_parte2.Controller;

import main.java.ass03_parte2.View.SimulationView;

public class Controller {

    private final SimulationView simulationView;

    public Controller(SimulationView simulationView) {
        this.simulationView = simulationView;
    }

    public SimulationView getSimulationView() {
        return this.simulationView;
    }

    public void createFrames() {
        this.simulationView.createFrames();
    }

    public void createControlFrame() {
        this.simulationView.createControl();
    }

}

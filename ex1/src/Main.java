package main.java.ass03_parte1;


import akka.actor.typed.ActorSystem;
import main.java.ass03_parte1.Controller.Controller;
import main.java.ass03_parte1.Model.MasterActor;
import main.java.ass03_parte1.Model.WorkerProtocol;
import main.java.ass03_parte1.View.SimulationView;

/*
Commment the first part to run the GUI versione of the program

Comment the second part to run the PERFORMANCE version of the program
 */
public class Main {
    public static void main(String[] args) {

        // TEST FOR PERFORMANCE
        ActorSystem<WorkerProtocol> masterActor = ActorSystem.create(MasterActor.create(null, false, 10000, 5000, 1), "MasterActor");
        masterActor.tell(new WorkerProtocol.BootMsg());

        // TEST FOR GUI
        //SimulationView viewer = new SimulationView(620,620);
        //Controller controller = new Controller(viewer, 1000, 1000, 5);
        //viewer.setController(controller);
    }
}

package main.java.ass03_parte1.Model;

import akka.actor.typed.ActorRef;
import java.util.ArrayList;

/*
Represent actions that a Worker can perform
*/
public interface WorkerProtocol {

    class BootMsg implements WorkerProtocol {
        public BootMsg(){}
    }

    class StopMsg implements WorkerProtocol {
        public StopMsg(){}
    }

    class UpdateVelocities implements WorkerProtocol {
        public final ActorRef<WorkerProtocol> masterActor;
        public final ArrayList<Body> bodies;
        public final double dt;

        public UpdateVelocities(ArrayList<Body> bodies, double dt, ActorRef<WorkerProtocol> masterActor) {
            this.bodies = bodies;
            this.dt = dt;
            this.masterActor = masterActor;
        }
    }

    class EndUpdateVelocities implements WorkerProtocol {
        public EndUpdateVelocities() {}
    }

    class UpdatePositionAndCheckBoundaryCollision implements WorkerProtocol {
        public final ActorRef<WorkerProtocol> masterActor;
        public final ArrayList<Body> bodies;
        public final double dt;
        public final Boundary bounds;

        public UpdatePositionAndCheckBoundaryCollision(ArrayList<Body> bodies, double dt, Boundary bounds, ActorRef<WorkerProtocol> masterActor) {
            this.bodies = bodies;
            this.dt = dt;
            this.bounds = bounds;
            this.masterActor = masterActor;
        }
    }

    class EndUpdatePositionAndCheckBoundaryCollision implements WorkerProtocol {
        public EndUpdatePositionAndCheckBoundaryCollision() {}
    }

}

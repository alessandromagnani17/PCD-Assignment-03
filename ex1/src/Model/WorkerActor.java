package main.java.ass03_parte1.Model;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.ArrayList;


public class WorkerActor extends AbstractBehavior<WorkerProtocol> {

    private final int startIndex;
    private final int numBallsForThisWorker;

    private WorkerActor(ActorContext<WorkerProtocol> context, int startIndex, int numBallsForThisWorker) {
        super(context);
        this.startIndex = startIndex;
        this.numBallsForThisWorker = numBallsForThisWorker;
    }

    /*
    Create a new worker actor
    */
    public static Behavior<WorkerProtocol> create(int startIndex, int numBallsForThisWorker) {
        return Behaviors.setup(context -> new WorkerActor(context, startIndex, numBallsForThisWorker));
    }

    @Override
    /*
    Worker's behaviour, that determine how to react on determined messages
    */
    public Receive<WorkerProtocol> createReceive() {
        return newReceiveBuilder()
                .onMessage(WorkerProtocol.StopMsg.class, this::onStopMsg)
                .onMessage(WorkerProtocol.UpdateVelocities.class, this::onUpdateVelocitiesMsg)
                .onMessage(WorkerProtocol.UpdatePositionAndCheckBoundaryCollision.class, this::onUpdatePositionAndCheckBoundaryCollision)
                .build();
    }

    /*
    Operations executed when user press the STOP button
    */
    private Behavior<WorkerProtocol> onStopMsg(WorkerProtocol.StopMsg msg) {
        return Behaviors.stopped();
    }

    /*
    Operation executed to update position and check boundary collisions
     */
    private Behavior<WorkerProtocol> onUpdatePositionAndCheckBoundaryCollision(WorkerProtocol.UpdatePositionAndCheckBoundaryCollision msg) {

        updatePosition(msg.bodies, msg.dt);

        checkBoundaryCollision(msg.bodies, msg.bounds);

        msg.masterActor.tell(new WorkerProtocol.EndUpdatePositionAndCheckBoundaryCollision());

        return this;
    }

    /*
    Updates all the workers positions
    */
    private void updatePosition(ArrayList<Body> bodies, double dt) {
        for (int i = this.startIndex; i < this.startIndex + this.numBallsForThisWorker; i++) {
            bodies.get(i).updatePos(dt);
        }
    }

    /*
    Checks all the workers boundary collisions
    */
    private void checkBoundaryCollision(ArrayList<Body> bodies, Boundary bounds){
        for (int i = this.startIndex; i < this.startIndex + this.numBallsForThisWorker; i++) {
            bodies.get(i).checkAndSolveBoundaryCollision(bounds);
        }
    }

    /*
    Operations executed to update all the velocities
     */
    private Behavior<WorkerProtocol> onUpdateVelocitiesMsg(WorkerProtocol.UpdateVelocities msg) {

        computeTotalForceAndUpdateVelocity(msg.bodies, msg.dt);

        msg.masterActor.tell(new WorkerProtocol.EndUpdateVelocities());

        return this;
    }

    /*
    Compute all total forces and updates all positions
    */
    private void computeTotalForceAndUpdateVelocity(ArrayList<Body> bodies, double dt){
        for (int i = this.startIndex; i < this.startIndex + this.numBallsForThisWorker; i++) {
            V2d totalForce = computeTotalForceOnBody(bodies, bodies.get(i));
            V2d acc = new V2d(totalForce).scalarMul(1.0 / bodies.get(i).getMass());
            bodies.get(i).updateVelocity(acc, dt);
        }
    }
    /*
    Computer the total force on a single body
     */
    private V2d computeTotalForceOnBody(ArrayList<Body> bodies, Body b) {
        V2d totalForce = new V2d(0, 0);

        for (int i = this.startIndex; i < this.startIndex + this.numBallsForThisWorker; i++)  {

            if (!b.equals(bodies.get(i))) {
                try {
                    V2d forceByOtherBody = b.computeRepulsiveForceBy(bodies.get(i));
                    totalForce.sum(forceByOtherBody);
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }
            } else {
            }
        }
        totalForce.sum(b.getCurrentFrictionForce());

        return totalForce;
    }

    private void log(String msg) {
        System.out.println("["+Thread.currentThread()+ "] " + msg);
    }
}

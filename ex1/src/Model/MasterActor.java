package main.java.ass03_parte1.Model;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import main.java.ass03_parte1.Controller.Controller;

import java.util.ArrayList;
import java.util.Random;

public class MasterActor extends AbstractBehavior<WorkerProtocol> {

    private final Boundary bounds = new Boundary(-4.0, -4.0, 4.0, 4.0);
    private final Controller controller;
    private final Boolean isGuiVersion;
    private final int nSteps;
    private final int nBalls;
    private final int nWorkers;
    private final double dt;
    private final Random rand;
    private final ArrayList<Body> bodies;
    private final ArrayList<ActorSystem<WorkerProtocol>> workersList;
    private int workersArrived = 0;
    private int actualSteps;
    private double vt;
    private long t0;

    private MasterActor(ActorContext<WorkerProtocol> context, Controller controller, boolean isGuiVersion, int nSteps, int nBalls, int nWorkers) {
        super(context);
        this.controller = controller;
        this.isGuiVersion = isGuiVersion;
        this.nSteps = nSteps;
        this.nBalls = nBalls;
        this.nWorkers = nWorkers;
        this.rand = new Random(System.currentTimeMillis());
        this.bodies = new ArrayList<>();
        this.workersList = new ArrayList<>();
        this.dt = 0.01;
    }

    /*
    Operation used for create workers
     */
    public static Behavior<WorkerProtocol> create(Controller controller, boolean isGuiVersion, int nSteps, int nBalls, int nWorkers) {
        return Behaviors.setup(context -> new MasterActor(context, controller, isGuiVersion, nSteps, nBalls, nWorkers));
    }

    @Override
    /*
    Master's behaviour, that determine how to react on determined messages
    */
    public Receive<WorkerProtocol> createReceive() {
        return newReceiveBuilder()
                .onMessage(WorkerProtocol.BootMsg.class, this::onBootMsg)
                .onMessage(WorkerProtocol.StopMsg.class, this::onStopMsg)
                .onMessage(WorkerProtocol.EndUpdateVelocities.class, this::onEndUpdateVelocities)
                .onMessage(WorkerProtocol.EndUpdatePositionAndCheckBoundaryCollision.class, this::onEndUpdatePositionAndCheckBoundaryCollision)
                .build();
    }

    /*
    Operations executed when user press the STOP button
    */
    private Behavior<WorkerProtocol> onStopMsg(WorkerProtocol.StopMsg msg) {

        for(int i = 0; i < this.nWorkers; i++){
            this.workersList.get(i).tell(new WorkerProtocol.StopMsg());
        }

        long t1 = System.currentTimeMillis() - this.t0;
        log("Execution ended | Time elapsed -> " + t1);

        return Behaviors.stopped();
    }

    /*
    if the workers arrived are equal to the number of total workers, they are displayed and then all the velocities are updated
    */
    private Behavior<WorkerProtocol> onEndUpdatePositionAndCheckBoundaryCollision(WorkerProtocol.EndUpdatePositionAndCheckBoundaryCollision msg) {
        this.workersArrived++;

        if(this.workersArrived == this.nWorkers){
            this.workersArrived = 0;
            this.actualSteps++;
            this.vt = this.vt + this.dt;

            if(this.isGuiVersion) this.controller.display(this.bodies, this.vt, this.actualSteps, this.bounds);

            if(this.actualSteps < this.nSteps){
                for(int j = 0; j < this.nWorkers; j++){
                    this.workersList.get(j).tell(new WorkerProtocol.UpdateVelocities(this.bodies, this.dt, this.getContext().getSelf()));
                }
            } else {

                long t1 = System.currentTimeMillis() - this.t0;
                log("Execution ended | Time elapsed -> " + t1);

                return Behaviors.stopped();
            }
        }

        return this;
    }

    /*
    if the workers arrived are equal to the number of total workers,they update their position and check boundary collisions
    */
    private Behavior<WorkerProtocol> onEndUpdateVelocities(WorkerProtocol.EndUpdateVelocities msg) {
        this.workersArrived++;

        if(this.workersArrived == this.nWorkers){
            this.workersArrived = 0;
            for(int j = 0; j < this.nWorkers; j++){
                this.workersList.get(j).tell(new WorkerProtocol.UpdatePositionAndCheckBoundaryCollision(this.bodies, this.dt, this.bounds, this.getContext().getSelf()));
            }
        }

        return this;
    }

    /*
    Actions that master does at first
    */
    private Behavior<WorkerProtocol> onBootMsg(WorkerProtocol.BootMsg msg) {

        log("Execution is starting...");

        for (int i = 0; i < this.nBalls; i++) {
            double x = this.bounds.getX0() * 0.25 + this.rand.nextDouble() * (this.bounds.getX1() - this.bounds.getX0()) * 0.25;
            double y = this.bounds.getY0() * 0.25 + this.rand.nextDouble() * (this.bounds.getY1() - this.bounds.getY0()) * 0.25;
            this.bodies.add(new Body(i, new P2d(x, y), new V2d(0, 0), 10));
        }

        createWorkerActorsAndAssignBalls();

        this.actualSteps = 0;
        this.vt = 0;
        this.t0 = System.currentTimeMillis();

        for(int j = 0; j < this.nWorkers; j++){
            this.workersList.get(j).tell(new WorkerProtocol.UpdateVelocities(this.bodies, this.dt, this.getContext().getSelf()));
        }

        return this;
    }


    /*
    Create all the workers and assign them all balls
    */
    private void createWorkerActorsAndAssignBalls() {
        ActorSystem<WorkerProtocol> workerActor;
        int ballsForCore = this.bodies.size() / this.nWorkers;
        int eventuallyRemainBalls = this.bodies.size() % this.nWorkers;
        int workerCreated = 0;
        int contTotalBalls = 0;
        boolean flag = true;

        while(contTotalBalls < this.bodies.size()){

            if(workerCreated >= (this.nWorkers - eventuallyRemainBalls) && flag){
                ballsForCore = ballsForCore + 1;
                flag = false;
            }

            //System.out.println("Creato worker | Start index: " + contTotalBalls + " | Num palline: " + ballsForCore);

            workerActor = createNewActor(contTotalBalls, ballsForCore, workerCreated);
            this.workersList.add(workerActor);

            contTotalBalls = contTotalBalls + ballsForCore;
            workerCreated = workerCreated + 1;
        }
    }


    private ActorSystem<WorkerProtocol> createNewActor(int startIndex, int numBallsForThisWorker, int workerID) {
        return ActorSystem.create(WorkerActor.create(startIndex, numBallsForThisWorker), "WorkerActor" + workerID);
    }

    private void log(String msg) {
        System.out.println("[MasterActor] " + msg);
    }

}

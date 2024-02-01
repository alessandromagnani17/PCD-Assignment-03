package main.java.ass03_parte2.Model;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import main.java.ass03_parte2.Controller.Controller;
import main.java.ass03_parte2.View.SimulationView;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Subscriber {

    private static final String EXCHANGE_NAME = "assignment03";
    private static final int FRAME_ROWS = 3;
    private static final int FRAME_COLUMNS = 3;
    private static final int PANEL_ROWS = 3;
    private static final int PANEL_COLUMNS = 2;
    private static Controller controller;
    private static int contSensors = 0;

    public static void main(String[] args) throws Exception {
        SimulationView simulationView = new SimulationView();
        Controller controller = new Controller(simulationView);

        /*
        Set the controller used as a "bridge" between Subscriber and SimulationView
        */
        setController(controller);

        /*
        Creates dinamically the GUI interface
        */
        controller.createFrames();

        /*
        Create the bottom panel that allows to do "Manage" or "Resolve" operation on an area
        */
        controller.createControlFrame();

        /*

        */
        createMOMParameters();
    }

    private static void setController(Controller controller1) {
        controller = controller1;
    }

    public static void createMOMParameters() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        DeliverCallback sensorCallback = createSensorCallback(channel);
        DeliverCallback firestationCallback = createFirestationCallback();

        declareQueueBindAndBasicConsume(channel, sensorCallback, firestationCallback);
    }

    private static void declareQueueBindAndBasicConsume(Channel channel, DeliverCallback sensorCallback, DeliverCallback firestationCallback) throws Exception {
        String queueNameSensor = channel.queueDeclare().getQueue();
        String queueNameFirestation = channel.queueDeclare().getQueue();

        for(int i = 0; i < FRAME_ROWS * FRAME_COLUMNS; i++) {
            for(int j = 0; j < PANEL_ROWS * PANEL_COLUMNS; j++){
                channel.queueBind(queueNameSensor, EXCHANGE_NAME, "" + i + "-" + contSensors);
                channel.basicConsume(queueNameSensor, true, sensorCallback, t -> {});

                contSensors++;
            }

            channel.queueBind(queueNameFirestation, EXCHANGE_NAME, "" + i);
            channel.basicConsume(queueNameFirestation, true, firestationCallback, t -> {});

            contSensors = 0;
        }
    }

    private static DeliverCallback createSensorCallback(Channel channel) {
        return (consumerTag, delivery) -> {

            int area = Integer.parseInt(String.valueOf(delivery.getEnvelope().getRoutingKey().charAt(0)));
            int sensor = Integer.parseInt(String.valueOf(delivery.getEnvelope().getRoutingKey().charAt(2)));

            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received on ['" + delivery.getEnvelope().getRoutingKey() + "]':'" + message + "'");

            if (Integer.parseInt(message) > 60) {

                if(!controller.getSimulationView().getMap().get(area).get(sensor).getBackground().equals(new Color(238, 200, 79))){
                    controller.getSimulationView().getMap().get(area).get(sensor).setBackground(new Color(196, 77, 77));
                }

                System.out.println("Area -> " + area + "| Sensore -> " + sensor);
            }

            if(checkForOtherSensor(area)){
                channel.basicPublish(EXCHANGE_NAME, "" + area, null, "Danger".getBytes("UTF-8"));
            }
        };
    }

    private static DeliverCallback createFirestationCallback() {
        return (consumerTag, delivery) -> {

            System.out.println("Firest ----------------> " + delivery.getEnvelope().getRoutingKey());

            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received on ['" + delivery.getEnvelope().getRoutingKey() + "]':'" + message + "'");

            if (!controller.getSimulationView().getPanelControl().get(Integer.parseInt(delivery.getEnvelope().getRoutingKey())).getBackground().equals(new Color(238, 200, 79)) ){
                controller.getSimulationView().getPanelControl().get(Integer.parseInt(delivery.getEnvelope().getRoutingKey())).setBackground(new Color(196, 77, 77));
            }

            controller.getSimulationView().getPanelControl().get(Integer.parseInt(delivery.getEnvelope().getRoutingKey())).getComponent(1).setEnabled(true);
        };
    }


    private static boolean checkForOtherSensor(int area) {
        int contActiveSensors = 0;

        for (JPanel p : controller.getSimulationView().getMap().get(area)) {

            if(p.getBackground().equals(new Color(196, 77, 77))){
                contActiveSensors++;
            }
        }

        System.out.println("Area " + area + " | attivi -> " + contActiveSensors + "| Totali/2 -> " + controller.getSimulationView().getMap().get(area).size() / 2);

        if(contActiveSensors > controller.getSimulationView().getMap().get(area).size() / 2) return true;

        return false;

    }
}

package main.java.ass03_parte2.Model;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;

public class Publisher {

    private static final int FRAME_ROWS = 3;
    private static final int FRAME_COLUMNS = 3;
    private static final int PANEL_ROWS = 3;
    private static final int PANEL_COLUMNS = 2;
    private static final String EXCHANGE_NAME = "assignment03";

    public static void main(String[] argv) throws Exception {

        /*
        Create a new ConnectionFactory
         */
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        /*
        Create a new connection
        */
        Connection connection = factory.newConnection();

        /*
        Create the channel for interact with the subscribers (sensors, zones...)
        */
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        /*
        Instance a random number that will be used in each subcriber to determine if that sensor will became red (rand > 60)
        */
        int randomNumber = 0;
        Random rand = new Random();

        Scanner keyboard = new Scanner(System.in);
        int myint = keyboard.nextInt();
        //int myint = 5;

        while (myint != 0) {
            Thread.sleep(3500);
            for(int i = 0; i < FRAME_ROWS * FRAME_COLUMNS; i++){
                for(int j = 0; j < PANEL_ROWS * PANEL_COLUMNS; j++){
                    randomNumber = rand.nextInt(100);
                    /*
                    Publish all the random numbers in the channel, and then the subscriber selecter as alarm sensor a sensor with random number > 60
                     */
                    channel.basicPublish(EXCHANGE_NAME, i + "-" + j, null, ("" + randomNumber).getBytes(StandardCharsets.UTF_8));
                    System.out.println("i = " + i + ", j = " + j + ", value: " + randomNumber);
                }
            }
            myint = keyboard.nextInt();
        }

        channel.close();
        connection.close();
    }

}

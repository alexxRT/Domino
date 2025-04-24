package client;
import java.io.*;

import connection.Connection;

public class Client {
    private Connection connRemote;
    private Thread handleIncomming;
    private Thread handleOutcomming;

    public void runNetworking(String ipAddress, int port) {
        connRemote = new Connection(ipAddress, port);
        // start handle incomming from server and send text to server
        if (connRemote.isConnected()) {
            handleIncomming = new Thread(new HandleIncomming());
            handleIncomming.start();

            handleOutcomming = new Thread(new HandleOutcomming());
            handleOutcomming.start();
        }
    }

    public void stopNetworking() {
        connRemote.tearConnection();
        try {
            handleIncomming.join();
            handleOutcomming.join();
        } catch (InterruptedException e) {
            System.out.println("Bad incomming handling thread join on exit!");
            e.printStackTrace();
        }
    }

    private class HandleIncomming implements Runnable {
        @Override
        public void run() {
            String serverMessage;
            while ((serverMessage = connRemote.recieveString()) != null) {
                // handle incomming messages here
                // expand it with switch-case further
                System.out.println("Recieved notification from server! Message: [" + serverMessage + "]");
                // Platform.runLater(() -> { - this is used to pass info on frontend
                // });
            }
        }
    }

    // simple buisness logic for client interface
    private class HandleOutcomming implements Runnable {
        @Override
        public void run() {
            try {
                while (connRemote.isConnected()) {
                    System.out.println("Please enter message you want to send to remote:");
                    String message = System.console().readLine();

                    connRemote.sendString(message);
                    System.out.println("Message sent!");
                }
            }
            catch (IOException e) {
                System.out.println("Unable to send message to remote!");
                e.printStackTrace();
            }
        }
    }
}
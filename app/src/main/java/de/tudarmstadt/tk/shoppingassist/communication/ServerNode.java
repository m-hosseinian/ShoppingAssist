package de.tudarmstadt.tk.shoppingassist.communication;

/**
 * Created by Mohammad on 6/27/2015.
 */

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerNode {

    private static String TAG = "ServerNode";

    private int portNumber;
    private MessageReceiverInterface receiver;
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private ServerService serverThread;
    private boolean connected;

    private static ServerNode instance;
    private boolean running;


    public static ServerNode getInstance(int portNumber) {
        if (instance == null) {
            instance = new ServerNode(portNumber);
        }
        return instance;
    }

    public void setReceiver(MessageReceiverInterface receiver) {
        this.receiver = receiver;
    }

    private ServerNode(int portNumber) {
        this.portNumber = portNumber;
        serverThread = new ServerService();
    }

    private class ServerService extends Thread {

        @Override
        public void run() {
            while (running) {
                try {
                    connected = false;
                    receiver.notifyUser("waiting for connection ...");

                    clientSocket = serverSocket.accept();
                    receiver.notifyUser("connection established.");

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    clientSocket.getInputStream()));
                    connected = true;
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        receiver.receive(inputLine);
                    }

                } catch (IOException e) {
                    receiver.notifyUser("disconnected!");
                    if (running) {
                        receiver.reestablishConnection();
                    }
                    Log.w(TAG, e.getMessage());
                }
            }
        }

        public void closeSocket() {
            try {
                serverSocket.close();
                serverSocket = null;
                if (connected) {
                    clientSocket.shutdownInput();
                    clientSocket.close();
                }
            } catch (IOException e) {
                Log.w(TAG, e.getMessage());
            }
        }
    }

    public void start() {
        running = true;
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            Log.w(TAG, e.getMessage());
        }
        if (!serverThread.isAlive())
            serverThread.start();
    }

    public void stop() {
        running = false;
        serverThread.closeSocket();
        serverThread = new ServerService();
    }
}
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

/**
 * This class acts as Socket server class. It opens a port and starts listening to it.
 * This class only receives data from counter part. This mechanism has been implemented
 * asynchronously using a new Thread. The client that is interested in the received data
 * should implement MessageReceiver interface register itself using serReceiver method.
 * This interface provides a receive method that is used by the server to send back the
 * information to the client.
 */
public class ServerNode {

    private static String TAG = "ServerNode";

    private int portNumber;
    private MessageReceiver receiver;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ServerService serverThread;
    private boolean connected;

    private static ServerNode instance;
    private boolean running;

    /**
     * Since one server instance should be created in our environment,
     * Singleton pattern is adopted.
     *
     * @param portNumber the port on which we want to start listening
     * @return ServerNode the handle for further stop/start command
     * on this object.
     */
    public static ServerNode getInstance(int portNumber) {
        if (instance == null) {
            instance = new ServerNode(portNumber);
        }
        return instance;
    }

    private ServerNode(int portNumber) {
        this.portNumber = portNumber;
        serverThread = new ServerService();
    }

    /**
     * Register receiver observer
     * @param receiver observer object that implements MessageReceiver
     */
    public void setReceiver(MessageReceiver receiver) {
        this.receiver = receiver;
    }

    /**
     * Avoids blocking other threads while listening
     */
    private class ServerService extends Thread {

        @Override
        public void run() {
            /* this flag keeps the thread alive. leave of the counterpart should not
             * cause server to stop. it lets the server starts listening form the beginning */
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
                        /* callback on observer object i.e. client */
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

        /**
         * Stops listening, closes the socket and frees all the resources i.e. port, I/OStream, etc.
         */
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

    /**
     * Creates new socket and runs the server thread that starts listening
     */
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

    /**
     * Causes the server thread to finish its job and terminate.
     * New Server Thread instance is created for later use i.e server start.
     */
    public void stop() {
        running = false;
        serverThread.closeSocket();
        serverThread = new ServerService();
    }
}
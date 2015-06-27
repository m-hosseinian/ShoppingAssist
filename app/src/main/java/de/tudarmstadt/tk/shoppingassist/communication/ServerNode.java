package de.tudarmstadt.tk.shoppingassist.communication;

/**
 * Created by Mohammad on 6/27/2015.
 */
import android.util.Log;

import java.io.*;
import java.net.*;

public class ServerNode {

    private static String TAG = "ServerNode";

    private int portNumber;
    private MessageReceiverInterface receiver;

    public ServerNode(int portNumber, MessageReceiverInterface receiver) {
        this.portNumber = portNumber;
        this.receiver = receiver;
    }

    public void run() {
        new Thread() {
            ServerSocket serverSocket;

            @Override
            public void run() {
                while (true) {
                    try {
                        serverSocket = new ServerSocket(portNumber);

                        receiver.notifyUser("waiting for connection ...");
                        Socket clientSocket = serverSocket.accept();
                        receiver.notifyUser("connection established.");

                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                        clientSocket.getInputStream()));

                        String inputLine;

                        while ((inputLine = in.readLine()) != null) {
                            receiver.receive(inputLine);
                        }

                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    } finally {
                        try {
                            receiver.notifyUser("disconnected!");
                            serverSocket.close();
                            receiver.reestablishConnection();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }
            }
        }.start();
    }
}

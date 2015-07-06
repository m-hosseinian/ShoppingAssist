package de.tudarmstadt.tk.shoppingassist.communication;

/**
 * Created by Mohammad on 6/27/2015.
 */

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientNode {

    private String hostName;
    private int portNumber;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private static boolean connected;

    private MessageReceiverInterface receiver;
    private static ClientNode instance;

    private static final String TAG = "ClientNode";

    public static ClientNode getInstance(String hostName, int portNumber) {
        if (instance == null) {
            instance = new ClientNode(hostName, portNumber);
        }
        return instance;
    }

    private ClientNode(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public void setReceiver(MessageReceiverInterface receiver) {
        this.receiver = receiver;
    }

    private class ClientAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            connected = false;
            receiver.notifyUser("looking for Gadgeteer ...");
            boolean polling = true;
            while (polling) {
                try {
                    clientSocket = new Socket(hostName, portNumber);
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(
                            new InputStreamReader(
                                    clientSocket.getInputStream()));
                    polling = false;
                } catch (IOException e) {
                    try {
                        Thread.sleep(200); // 0.2 seconds
                    } catch (InterruptedException ie) {
                        Log.w(TAG, ie.getMessage());
                    }
                }
            }

            connected = true;
            receiver.notifyUser("connected.");
            String receivedMessage;

            try {
                while ((receivedMessage = in.readLine()) != null) {
                    receiver.receive(receivedMessage);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                stop();
                start();
            }

            return null;
        }
    }

    public void send(String message) {
        out.println(message);
    }

    public boolean isConnected() {
        return connected;
    }

    public void start() {
        new ClientAsyncTask().execute();
    }

    public void stop() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            Log.w(TAG, e.getMessage());
        }
        out.flush();
        out.close();
    }
}

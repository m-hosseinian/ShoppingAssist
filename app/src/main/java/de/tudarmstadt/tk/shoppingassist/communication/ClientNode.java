package de.tudarmstadt.tk.shoppingassist.communication;

/**
 * Created by Mohammad on 6/27/2015.
 */

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientNode {

    private String hostName;
    private int portNumber;

    private Socket clientSocket;
    private PrintWriter out;
    private boolean connected;

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

    private class ClientAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            connected = false;
        }

        @Override
        protected Void doInBackground(Void... params) {
            boolean polling = true;
            while (polling) {
                try {
                    clientSocket = new Socket(hostName, portNumber);
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    polling = false;
                } catch (IOException e) {
                    try {
                        Thread.sleep(200); // 0.2 seconds
                    } catch (InterruptedException ie) {
                        Log.w(TAG, ie.getMessage());
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            connected = true;
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

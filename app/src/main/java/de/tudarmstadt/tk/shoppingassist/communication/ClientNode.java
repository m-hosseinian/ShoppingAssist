package de.tudarmstadt.tk.shoppingassist.communication;

/**
 * Created by Mohammad on 6/27/2015.
 */

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This class acts as Socket client class. It attempts to connect to a given socket address.
 * This class only sends data to the counterpart. As part of the API it exposes a send method
 * that is used by the client to send information to the counter part listening on specified
 * socket.
 */
public class ClientNode {

    private String hostName;
    private int portNumber;

    private Socket clientSocket;
    private PrintWriter out;
    private boolean connected;

    private static ClientNode instance;
    private final String TAG = "ClientNode";

    /**
     * Since one client instance should be created in our environment,
     * Singleton pattern is adopted.
     *
     * @param hostName counterpart host address
     * @param portNumber counterpart port number
     * @return ClientNode the handle for further stop/start command
     * on this object.
     */
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

    /* networking tasks on Android conventionally is done in separate AsyncTask */
    private class ClientAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            connected = false;
        }

        /* attempts to connect to counterpart */
        @Override
        protected Void doInBackground(Void... params) {
            /* this flag keeps the thread alive. absence of the counterpart should not
             * cause client to stop. it lets the client keeps attempting to connect
             * until counterpart arrives. */
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
            Log.i(TAG, "client connected.");
        }
    }

    /**
     * Sends data to counterpart.
     */
    public void send(String message) {
        out.println(message);
    }

    /**
     * Checks if the socket client is connected to the counterpart.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Creates new AsyncTask and runs it to start attempting to connect.
     */
    public void start() {
        new ClientAsyncTask().execute();
    }

    /**
     * Closes the connect force flush all the
     * remainder data on the channel.
     */
    public void stop() {
        if (connected) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Log.w(TAG, e.getMessage());
            }

            out.flush();
            out.close();
        }
    }
}
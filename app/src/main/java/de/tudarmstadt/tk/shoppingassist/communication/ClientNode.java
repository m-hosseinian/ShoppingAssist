package de.tudarmstadt.tk.shoppingassist.communication;

/**
 * Created by Mohammad on 6/27/2015.
 */

import android.os.AsyncTask;

import java.io.*;
import java.net.*;

public class ClientNode {

    private Socket clientSocket;
    private PrintWriter out;
    private boolean connected;

    public ClientNode(String hostName, int portNumber) {

        new ClientAsyncTask(hostName, portNumber).execute();
    }

    private class ClientAsyncTask extends AsyncTask<Void, Void, Void> {

        private String hostName;
        private int portNumber;

        public ClientAsyncTask(String hostName, int portNumber) {
            this.hostName = hostName;
            this.portNumber = portNumber;
        }

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
                        ie.printStackTrace();
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
}

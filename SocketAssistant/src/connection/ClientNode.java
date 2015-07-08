package connection;

import java.io.*;
import java.net.*;

public class ClientNode {

	private Socket clientSocket;
	private PrintWriter out;
	private boolean connected;

	public ClientNode(String hostName, int portNumber) {
		connected = false;
		new Thread() {
			@Override
			public void run() {
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
				connected = true;
			}
		}.start();
	}

	public void send(String message) {
		out.println(message);
	}

	public boolean isConnected() {
		return connected;
	}
}
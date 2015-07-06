package connection;

import java.io.*;
import java.net.*;

public class ServerNode {

	private int portNumber;
	private MessageReceiver receiver;
	private boolean isConnected = false;
	private Socket clientSocket;
	private PrintWriter out;

	public boolean isConnected() {
		return isConnected;
	}

	public void send(String message) {
		out.println(message);
	}

	public ServerNode(int portNumber, MessageReceiver receiver) {
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
						System.out.println("waiting for connection ...");
						isConnected = false;

						clientSocket = serverSocket.accept();
						System.out.println("connection established.");
						isConnected = true;

						BufferedReader in = new BufferedReader(
								new InputStreamReader(
										clientSocket.getInputStream()));

						out = new PrintWriter(clientSocket.getOutputStream(),
								true);

						String inputLine;

						while ((inputLine = in.readLine()) != null) {
							receiver.receive(inputLine);
						}

					} catch (IOException e) {
						System.out.println(e.getMessage());
					} finally {
						try {
							System.out.println("disconnected!");
							serverSocket.close();
							receiver.reestablishConnection();
						} catch (IOException e) {
						}
					}
				}
			}
		}.start();
	}
}

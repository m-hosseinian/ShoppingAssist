package connection;

import java.io.*;
import java.net.*;

public class ServerNode {

	private int portNumber;
	private MessageReceiver receiver;

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

						Socket clientSocket = serverSocket.accept();
						System.out.println("connection established.");

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
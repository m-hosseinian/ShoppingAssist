package cnode;
import java.io.*;

import connection.*;

public class CommunicationNode implements MessageReceiver {

	private static int localServerPort;
	private static String remoteServerhost;
	private static int remoteServerPort;
	
	private static ClientNode client;
	
	public static void main(String[] args) {

		if (args.length != 3) {
			System.err.println("Usage: java Node <local port> <remote host> <remote port>");
			System.exit(1);
		}

		CommunicationNode node = new CommunicationNode();
		localServerPort = Integer.parseInt(args[0]);
		remoteServerhost = args[1];
		remoteServerPort = Integer.parseInt(args[2]);
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(
				System.in));

		new ServerNode(localServerPort, node).run();

		stablishConnection();

		String userInput;
		try {
			while ((userInput = stdIn.readLine()) != null) {
				
				if (client.isConnected()) {
					client.send(userInput);
					System.out.println("me: " + userInput);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void stablishConnection() {
		client = new ClientNode(remoteServerhost, remoteServerPort);
	}
	
	@Override
	public void receive(String message) {
		System.out.println("counterpart: " + message);
	}

	@Override
	public void reestablishConnection() {
		stablishConnection();
	}
}

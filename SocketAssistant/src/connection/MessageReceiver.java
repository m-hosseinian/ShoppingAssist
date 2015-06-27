package connection;

public interface MessageReceiver {
	void receive(String message);
	void reestablishConnection();
}

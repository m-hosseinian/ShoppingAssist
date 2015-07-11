package de.tudarmstadt.tk.shoppingassist.communication;

/**
 * Created by Mohammad on 6/27/2015.
 */
public interface MessageReceiver {
    void receive(String message);
    void reestablishConnection();
    void notifyUser(String message);
}

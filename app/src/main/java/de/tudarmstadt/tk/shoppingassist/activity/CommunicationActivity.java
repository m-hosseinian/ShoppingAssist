package de.tudarmstadt.tk.shoppingassist.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import de.tudarmstadt.tk.shoppingassist.R;
import de.tudarmstadt.tk.shoppingassist.communication.ClientNode;
import de.tudarmstadt.tk.shoppingassist.communication.MessageReceiverImpl;
import de.tudarmstadt.tk.shoppingassist.communication.MessageReceiverInterface;
import de.tudarmstadt.tk.shoppingassist.communication.ServerNode;

public class CommunicationActivity extends ActionBarActivity {

    private static String TAG = "CommunicationActivity";

    private final int LOCAL_PORT = 5000;
    private final String REMOTE_HOST = "192.168.1.11" ;
    private final int REMOTE_PORT = 6000;

    private ScrollView chatScrollView;
    private TextView chatTextView;
    private TextView messageEditText;
    private Button sendButton;

    private ClientNode client;
    private ServerNode server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);

        /* code starts from here */

        chatTextView = (TextView) findViewById(R.id.textViewChat);
        messageEditText = (EditText) findViewById(R.id.textViewMessage);
        sendButton = (Button) findViewById(R.id.buttonSend);
        chatScrollView = (ScrollView) findViewById(R.id.scrollViewChat);

        initButtons();

        server = ServerNode.getInstance(LOCAL_PORT);
        server.setReceiver(node);

        client = ClientNode.getInstance(REMOTE_HOST, REMOTE_PORT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        client.start();
        server.start();
    }

    @Override
    protected void onPause() {
        client.stop();
        server.stop();
        super.onPause();
    }

    private void printOnChatTextView(final String message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                chatTextView.setText(chatTextView.getText().toString() +
                        message + "\n");
                chatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    void initButtons() {

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 /* if you wrote something in the edit text */
                if (messageEditText.getText() != null &&
                        messageEditText.getText().length() != 0) {

                    printOnChatTextView("me: " +
                            messageEditText.getText().toString());
                } else {
                    Log.w(TAG, "empty message.");
                }
                if (client.isConnected()) {
                    client.send(messageEditText.getText().toString());
                } else {
                    Log.w(TAG, "client is not connected!");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /* reset the edit text */
                        messageEditText.setText("");
                    }
                });
            }
        });
    }

    private MessageReceiverInterface node = new MessageReceiverImpl() {

        @Override
        public void receive(String message) {
            printOnChatTextView("counterpart: " + message);
        }

        @Override
        public void reestablishConnection() {
            client.start();
        }

        @Override
        public void notifyUser(String message) {
            printOnChatTextView(message);
        }
    };
}



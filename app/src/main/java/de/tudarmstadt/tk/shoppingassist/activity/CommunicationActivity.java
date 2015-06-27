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

        new ServerNode(LOCAL_PORT, node).run(); /* always up and running server */

        establishConnection(); /* for the first time try to reach the counterpart */
    }

    void initButtons() {

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /* if you wrote something in the edit text */
                        if (messageEditText.getText() != null && messageEditText.getText().length() != 0) {
                            /* show the message in the text view */
                            chatTextView.setText(chatTextView.getText().toString() +
                                    "me : " + messageEditText.getText() +
                                    "\n");
                            /* send the message */
                            if (client.isConnected()) {
                                client.send(messageEditText.getText().toString());
                            }
                            /* reset the edit text */
                            messageEditText.setText("");
                        } else {
                            Log.w(TAG, "empty message.");
                        }
                        /* scroll down to always show the latest messages */
                        chatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    private void establishConnection() {
        client = new ClientNode(REMOTE_HOST, REMOTE_PORT);
    }

    private MessageReceiverInterface node = new MessageReceiverImpl() {
        private String lmsg;

        @Override
        public void receive(String message) {
            lmsg = message;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatTextView.setText(chatTextView.getText().toString() +
                            "counterpart: " + lmsg + "\n");
                    chatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }

        @Override
        public void reestablishConnection() {
            establishConnection();
        }

        @Override
        public void notifyUser(String message) {
            lmsg = message;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatTextView.setText(chatTextView.getText().toString() +
                            lmsg + "\n");
                    chatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }
    };
}



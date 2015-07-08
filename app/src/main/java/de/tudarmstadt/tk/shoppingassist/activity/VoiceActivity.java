package de.tudarmstadt.tk.shoppingassist.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.tudarmstadt.tk.shoppingassist.R;
import de.tudarmstadt.tk.shoppingassist.communication.ClientNode;
import de.tudarmstadt.tk.shoppingassist.communication.MessageReceiverImpl;
import de.tudarmstadt.tk.shoppingassist.communication.MessageReceiverInterface;
import de.tudarmstadt.tk.shoppingassist.communication.ServerNode;


/**
 * Created by babak on 06.07.15.
 */
public class VoiceActivity extends ActionBarActivity  {

    private static String TAG = "VoiceActivity";

    private String finalOrder;

    static final int check = 1111;
    public String remoteIp;
    private final int LOCAL_PORT = 8081;
    private final int REMOTE_PORT = 8080;
    private ClientNode client;
    private ServerNode server;
    private HashMap<String,String> DATABASE = new HashMap<>();
    private List<String> orders = new ArrayList<>();
    private TextToSpeech speaker;
    Vibrator vibrator;

    private Button btnVoice;
    private TextView ordersTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        speaker = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    speaker.setLanguage(Locale.US);
                    speaker.setSpeechRate(0.8f);
                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    audioManager.setSpeakerphoneOn(true);
                }
            }
        });
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        /**
         * Initialization of our virtual Database.
         */
        DATABASE.put("milk", "4D00556F06");
        DATABASE.put("potato","4D00556F07");
        DATABASE.put("tomato","4D00556F08");
        DATABASE.put("bread","4D00556F09");
        DATABASE.put("bacon","4D00556F10");
        DATABASE.put("onion","4D00556F11");


        remoteIp = intent.getStringExtra("ip");

        server = ServerNode.getInstance(LOCAL_PORT);
        server.setReceiver(node);

        client = ClientNode.getInstance(remoteIp, REMOTE_PORT);

        btnVoice = (Button) findViewById(R.id.btnVoice);
        ordersTextView = (TextView) findViewById(R.id.textViewOrders);

        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk!");
                startActivityForResult(i, check);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == check && resultCode == RESULT_OK) {
            if (data != null) {
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                //get the text from speech
                Toast.makeText(getApplicationContext(), result.get(0), Toast.LENGTH_LONG).show();
                Log.i("Your speech", result.get(0));
                //Extract order items
                String[] elements = result.get(0).split(" ");
                Log.i("Speech length: ", String.valueOf(elements.length));
                //compare each word against goods and digits

                for (int i = 0; i < elements.length; i++) {
                    if (DATABASE.containsKey(elements[i])) {
                        printOnOrdersTextView(elements[i]);
                        orders.add(DATABASE.get(elements[i]));
                    }
                }
                if (!orders.isEmpty()) {
                    notifyShoppingCart();
                    orders = new ArrayList<>();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void printOnOrdersTextView(final String message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ordersTextView.setText(ordersTextView.getText().toString() +
                        message + "\n");

            }
        });
    }

    private class TransmissionThread extends Thread {

        @Override
        public void run() {
            Log.i(TAG, "about to send: " + finalOrder);
            while (!client.isConnected()){
                try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
            client.send(finalOrder);
            Log.i(TAG, "sent.");
        }
    }

    private void notifyShoppingCart() {
        finalOrder = "";
        for (String item : orders ) {
            finalOrder = item + ";" + finalOrder;
        }
        new TransmissionThread().start();
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

    private MessageReceiverInterface node = new MessageReceiverImpl() {

        @Override
        public void receive(String message) {
            Iterator iterator = DATABASE.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<String,String> item = (Map.Entry<String, String>) iterator.next();
                if (item.getValue().equals(message)){
                    vibrator.vibrate(300);
                    speaker.speak(item.getKey() + "found", TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        }

        @Override
        public void reestablishConnection() {
            client.start();
        }

        @Override
        public void notifyUser(String message) {
            Log.i(TAG, "Communication: " + message);
        }
    };
}

package de.tudarmstadt.tk.shoppingassist.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.support.v7.app.ActionBarActivity;
import de.tudarmstadt.tk.shoppingassist.R;
import de.tudarmstadt.tk.shoppingassist.communication.ClientNode;
import de.tudarmstadt.tk.shoppingassist.communication.MessageReceiverImpl;
import de.tudarmstadt.tk.shoppingassist.communication.MessageReceiverInterface;
import de.tudarmstadt.tk.shoppingassist.communication.ServerNode;


/**
 * Created by babak on 06.07.15.
 */
public class VoiceActivity extends ActionBarActivity  {

    static final int check = 1111;
    public String REMOTE_IP;
    public int REMOTE_PORT = 5000;
    private ClientNode client;
    private ServerNode server;
    private HashMap<String,String> DATABASE = new HashMap<String,String>();
    private List<String> orders = new ArrayList<String>();
    private TextToSpeech speaker;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        speaker=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    speaker.setLanguage(Locale.UK);
                }
            }
        });
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        /**
         * Initialization of our virtual Database.
         */
        DATABASE.put("XXX111","milk");
        DATABASE.put("XXX112","tomato");
        DATABASE.put("XXX113","bread");
        DATABASE.put("XXX114","strawberry");
        DATABASE.put("XXX115","bacon");
        DATABASE.put("XXX116","noodle");
        DATABASE.put("XXX117","toilet paper");
        DATABASE.put("XXX118","potato");


        REMOTE_IP = intent.getStringExtra("ip");

        client = ClientNode.getInstance(REMOTE_IP, REMOTE_PORT);
        client.setReceiver(node);

        Button btnVoice = (Button) findViewById(R.id.btnVoice);
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
                int j;
                for (int i = 0; i < elements.length; i++)
                    if(DATABASE.containsKey(elements[i]))
                        orders.add(elements[i]);
                if (!orders.isEmpty())
                    notifyShoppingCart();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void notifyShoppingCart() {
        String finalOrder = "";
        Iterator it = orders.iterator();
        while (it.hasNext()) {
            String item = (String) it.next();
            if (finalOrder.equals(""))
                finalOrder = item;
            else
                finalOrder = finalOrder + ";" + item;
        }
        client.send(finalOrder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (client != null)
            client.start();
        //server.start();
    }

    @Override
    protected void onPause() {
        if(speaker !=null){
            speaker.stop();
            speaker.shutdown();
        }
        if (client != null )
            client.stop();
        //server.stop();
        super.onPause();
    }

    private MessageReceiverInterface node = new MessageReceiverImpl() {

        @Override
        public void receive(String message) {
            vibrator.vibrate(300);
            speaker.speak(message,TextToSpeech.QUEUE_FLUSH,null,null);

        }

        @Override
        public void reestablishConnection() {

        }

        @Override
        public void notifyUser(String message) {
            speaker.speak(message,TextToSpeech.QUEUE_FLUSH,null,null);

        }
    };
}

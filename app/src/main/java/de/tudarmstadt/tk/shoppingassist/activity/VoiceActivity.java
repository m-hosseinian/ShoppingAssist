package de.tudarmstadt.tk.shoppingassist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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
import java.util.Map;
import android.support.v7.app.ActionBarActivity;
import de.tudarmstadt.tk.shoppingassist.R;


/**
 * Created by babak on 06.07.15.
 */
public class VoiceActivity extends ActionBarActivity  {

    static final int check = 1111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnVoice = (Button) findViewById(R.id.btnVoice);
        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak up!");
                startActivityForResult(i, check);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == check && resultCode == RESULT_OK) {
            if (data != null) {
                //get the text from speech
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Toast.makeText(getApplicationContext(), result.get(0), Toast.LENGTH_LONG).show();
                Log.i("Your speech", result.get(0));
                //Extract order items
                HashMap<String, String> order = goodsSeperator(result.get(0));
                //to show result
                Iterator it = order.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    Log.i("Your Order ", pair.getKey() + " = " + pair.getValue());
                    it.remove(); // avoids a ConcurrentModificationException
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected HashMap<String, String> goodsSeperator(String input) {

        if (input.length() < 1)
            return new HashMap<String, String>();
        //pre defined goods
        String[] g = {"milk", "rice", "potato", "tomato", "meat", "bread", "cheese", "butter"};
        ArrayList<String> goods = new ArrayList<String>(Arrays.asList(g));
        String[] d = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};
        ArrayList<String> digits = new ArrayList<String>(Arrays.asList(d));

        HashMap<String, String> orders = new HashMap<String, String>();
        //convert input string to array of words
        String[] elements = input.split(" ");
        Log.i("Speech length: ", String.valueOf(elements.length));
        //compare each word against goods and digits
        int j;
        for (int i = 0; i < elements.length; i++)
            if (digits.contains(elements[i]))
                for (j = i; j < elements.length; j++)
                    if (goods.contains(elements[j])) {
                        orders.put(elements[j], elements[i]);
                        Log.i(elements[j], " : " + elements[i]);
                        i = j; break;
                    }
        return orders;
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
}

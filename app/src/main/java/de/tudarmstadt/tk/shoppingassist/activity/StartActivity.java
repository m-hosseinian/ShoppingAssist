package de.tudarmstadt.tk.shoppingassist.activity;

/**
 * Created by babak on 06.07.15.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tudarmstadt.tk.shoppingassist.R;

public class StartActivity extends Activity {

    private EditText edittext;
    private Pattern pattern;
    private Matcher matcher;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    }


    public void sendMessage(View view) {
        // Go to voice activity
        Log.i("StartActivity","Sending IP to another activity");
        String IPADDRESS_PATTERN =
                "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

        pattern = Pattern.compile(IPADDRESS_PATTERN);


        Intent intent = new Intent(this, VoiceActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        matcher = pattern.matcher(message);
        if (matcher.matches()) {
            intent.putExtra("ip", message);
            startActivity(intent);
        }
        else {
            Toast.makeText(getApplicationContext(),"Please enter a valid ip address",Toast.LENGTH_SHORT).show();
        }

    }
}
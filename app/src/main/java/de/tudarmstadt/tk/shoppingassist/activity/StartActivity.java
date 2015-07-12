package de.tudarmstadt.tk.shoppingassist.activity;

/**
 * Created by babak on 06.07.15.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tudarmstadt.tk.shoppingassist.R;
import de.tudarmstadt.tk.shoppingassist.communication.ClientNode;

public class StartActivity extends Activity {

    private final int REMOTE_PORT = 8080;
    private ClientNode client;
    private String message;
    private Pattern pattern;
    private Matcher matcher;
    private EditText editText;

    private ConnectionTask connectionTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        editText = (EditText) findViewById(R.id.editText);
    }

    /**
     * Check if the entered IP address is correct.
     */
    private class ConnectionThread extends Thread {

        @Override
        public void run() {
            client = new ClientNode(message, REMOTE_PORT);
            client.start();
        }
    }

    /**
     * displays a ProgressDialog
     */
    private class ConnectionTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(StartActivity.this, "", "Connecting...");
            new ConnectionThread().start();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(Void param) {

            progressDialog.dismiss();

            if (client.isConnected()) {

                Toast.makeText(StartActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(StartActivity.this, VoiceActivity.class);
                intent.putExtra("ip", message);
                startActivity(intent);
            } else {
                Toast.makeText(StartActivity.this, "Connection failed.", Toast.LENGTH_SHORT).show();
            }
            client.stop();
        }
    }

    /**
     * The app start here and checks the field for a Valid IP address and then passes it to the mainactivity.
     * Before passing the IP to the main Activity it is checked whether it is a valid input ir not.
     * @param view
     */
    public void sendMessage(View view) {
        // Go to voice activity
        Log.i("StartActivity","Sending IP to another activity");
        String IPADDRESS_PATTERN =
                "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

        pattern = Pattern.compile(IPADDRESS_PATTERN);
        message = editText.getText().toString();
        matcher = pattern.matcher(message);
        if (matcher.matches()) {
            connectionTask = new ConnectionTask();
            connectionTask.execute();
        }
        else {
            Toast.makeText(getApplicationContext(),"Please enter a valid ip address",Toast.LENGTH_SHORT).show();
        }
    }
}
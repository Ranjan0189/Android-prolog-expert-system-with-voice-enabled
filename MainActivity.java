package com.example.root.myapplication90;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.content.ActivityNotFoundException;
import android.speech.RecognizerIntent;
import java.util.ArrayList;
import java.util.Locale;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;



public class MainActivity extends Activity implements TCPListener {

    TextToSpeech toSpeech;
    String text;
    int result;

    private final int REQ_CODE_SPEECH_INPUT = 100;
    private TCPCommunicator tcpClient;
    private ProgressDialog dialog;
    public static String currentUserName;
    private Handler UIHandler = new Handler();
    private boolean isFirstLoad=true;
    private String obj1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toSpeech=new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status==TextToSpeech.SUCCESS)
                {
                    result=toSpeech.setLanguage(Locale.UK);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Feature not supported in your device",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void connectToServer(View view) {
        setupDialog();
        tcpClient = TCPCommunicator.getInstance();
        TCPCommunicator.addListener(this);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        tcpClient.init(settings.getString(EnumsAndStatics.SERVER_IP_PREF, "192.168.43.5"),
                Integer.parseInt(settings.getString(EnumsAndStatics.SERVER_PORT_PREF, "3050")));
    }

    private void setupDialog() {
        dialog = new ProgressDialog(this,ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Loading");
        dialog.setMessage("Please wait, connecting to server...");
        dialog.setIndeterminate(true);
        dialog.show();
    }

    public void about(View view){
        Toast.makeText(this, "This app is created by Ranjan Kumar Mandal in his AI project with Kuntal sir", Toast.LENGTH_SHORT).show();
    }

    public void getSpeechInput(View view) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        EditText txtName= (EditText)findViewById(R.id.txtUserName);
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtName.setText(result.get(0));
                }
                break;
        }

        btnSendClick();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_activity2, menu);
        return true;
    }

    @Override
    protected void onStop()
    {
        super.onStop();

    }


    public void btnSendClick()
    {
        EditText txtName= (EditText)findViewById(R.id.txtUserName);

        if(txtName.getText().toString().length()==0)
        {
            Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show();
            return;
        }
        try
        {
           // obj.put(EnumsAndStatics.MESSAGE_TYPE_FOR_JSON, EnumsAndStatics.MessageTypes.MessageFromClient);
            obj1 = txtName.getText().toString();    //HERE i have to instialise for array of string
        }                                           
        catch(Exception e)
        {
            e.printStackTrace();
        }
        TCPCommunicator.writeToSocket(obj1,UIHandler,this); //HERE i have to create firstly an
        //dialog.show();                                   //array then to send it at place of obj1

    }

    @Override
    public void onTCPMessageRecieved(String message) {
        final String theMessage=message;
        final EditText editTextFromServer =(EditText)findViewById(R.id.editTextFromServer);



        runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            editTextFromServer.setText(theMessage);

                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                            {
                                Toast.makeText(getApplicationContext(),"Feature not supported in your device",Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                text = editTextFromServer.getText().toString();
                                toSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);
                            }
                        }
                    });

    }

    @Override
    public void onTCPConnectionStatusChanged(boolean isConnectedNow) {
        if(isConnectedNow)
        {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    dialog.hide();
                    Toast.makeText(getApplicationContext(), "Connected to server", Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    public void btnSettingsClicked(View view)
    {
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toSpeech!=null)
        {
            toSpeech.stop();
            toSpeech.shutdown();
        }
    }
}


package com.example.sam33r.mainapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final String DEVICE_ADDRESS = "00:21:13:01:EB:50"; //MAC Address of Bluetooth Module
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final int ON =1;
    private static final int OFF=0;

    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    public String voice;
    private TextView resultTV;

    private Button button_on, button_off,  bluetooth_connect_btn, button_speak;

    public String command; //char to be sent to serial
    public void promtSpeechInput(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"say something :)");

        try {
            startActivityForResult(i, 100);
        }catch (ActivityNotFoundException a) {
            Toast.makeText(MainActivity.this,"some error",Toast.LENGTH_LONG).show();
        }
    }
    public void onActivityResult( int request_code, int result_code, Intent i) {
        super.onActivityResult(request_code, result_code, i);
        switch (request_code) {
            case 100:
                if (result_code == RESULT_OK && i != null) {
                    ArrayList<String> result = i.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    voice = result.get(0);

                    stringToCommand(voice);
                   resultTV.setText(voice);
                }

                break;
        }

    }

    public void stringToCommand(String Input){
        boolean errorMessage = true;

        if(voice.contains("jarvis")){
            String regex = "\\s*\\bjarvis\\b\\s*";
            voice.replaceAll(regex,"");
            if(voice.contains("computer room")){
                regex = "\\s*\\bcomputer room\\b\\s*";
                voice.replaceAll(regex,"");
                if(voice.contains("light")){
                    regex= "\\s*\\blight\\b\\s*";
                    voice.replaceAll(regex,"");
                    if(voice.contains("off") ){
                        sendToArduino(1,"2");
                    }
                    else if (voice.contains("on")){
                        sendToArduino(1,"1");
                    }

                }
                else if(voice.contains("fan")){
                    regex= "\\s*\\bfan\\b\\s*";
                    voice.replaceAll(regex,"");
                    if(voice.contains("off")){
                        sendToArduino(2,"4");
                    }
                    else if (voice.contains("on")){
                        sendToArduino(2,"3");
                    }

                }
            }

        }






    }

    void sendToArduino(int relay , String send){

        try
        {
            outputStream.write(send.getBytes());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //declaration of button variables
        button_on = (Button) findViewById(R.id.button_on);
        button_off = (Button) findViewById(R.id.button_off);
        button_speak = (Button) findViewById(R.id.button_speak);

        bluetooth_connect_btn = (Button) findViewById(R.id.button_connct);
        resultTV = (TextView) findViewById(R.id.textview_result);




       button_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                command = "3";

                try
                {
                    outputStream.write(command.getBytes()); //transmits the value of command to the bluetooth module
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        });

        button_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                command = "4";

                try
                {
                    outputStream.write(command.getBytes());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        bluetooth_connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(BTinit())
                {
                    BTconnect();
                }

            }
        });

        button_speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                promtSpeechInput();



            }
        });



        //Button that connects the device to the bluetooth module when pressed


    }

    //Initializes bluetooth module
    public boolean BTinit()
    {
        boolean found = false;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null) //Checks if the device supports bluetooth
        {
            Toast.makeText(getApplicationContext(), "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
        }

        if(!bluetoothAdapter.isEnabled()) //Checks if bluetooth is enabled. If not, the program will ask permission from the user to enable it
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter,0);

            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        if(bondedDevices.isEmpty()) //Checks for paired bluetooth devices
        {
            Toast.makeText(getApplicationContext(), "Please pair the device first", Toast.LENGTH_SHORT).show();
        }
        else
        {
            for(BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    public boolean BTconnect()
    {
        boolean connected = true;

        try
        {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID); //Creates a socket to handle the outgoing connection
            socket.connect();

            Toast.makeText(getApplicationContext(),
                    "Connection to bluetooth device successful", Toast.LENGTH_LONG).show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            connected = false;
        }

        if(connected)
        {
            try
            {
                outputStream = socket.getOutputStream(); //gets the output stream of the socket
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        return connected;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

}
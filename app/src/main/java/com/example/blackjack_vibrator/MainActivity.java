package com.example.blackjack_vibrator;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.DataInputStream;
import java.net.Socket;



import android.view.View;

import android.os.AsyncTask;

import android.util.Log;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
    Button moveBtn;
    EditText txtAddress;
    public static String wifiModuleIp = "";
    public static int wifiModulePort = 21567;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        moveBtn = (Button) findViewById(R.id.button2);
        txtAddress = (EditText) findViewById(R.id.editTextText);
        moveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                on click set up the server address and try to connect to that address
                wifiModuleIp = txtAddress.getText().toString();
                Socket_AsyncTask phone_socket = new Socket_AsyncTask();
                phone_socket.execute();
            }
        });

    }
    public class Socket_AsyncTask extends AsyncTask<Void,Void,Void>
    {
        Socket socket;

        @Override
        protected Void doInBackground(Void... params){
            try{
//                connect and send get move to the server
                InetAddress inetAddress = InetAddress.getByName(MainActivity.wifiModuleIp);
                socket = new java.net.Socket(inetAddress,MainActivity.wifiModulePort);
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream.writeUTF("Get Move");

//                read what the server replys with
                String line = (String) dataInputStream.readUTF();
//                not running on main thread so use this function to run on the main thread so we can access vibration and screen elements
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {


                        int next_move = Integer.parseInt(line);

                        // Stuff that updates the UI
                        TextView textView = (TextView) findViewById(R.id.textView);
                        textView.setText(line);

                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        // make vibration pattern that vibrates the amount of times sent by the server
                        int[] amplitudes = new int[next_move*2];

                        for (int i = 0; i < next_move; i++) {
                            amplitudes[i*2] = 255;
                        }

                        long[] timings = new long[next_move*2];
                        Arrays.fill(timings, 300);

                        int repeatIndex = -1; // Do not repeat.

                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, repeatIndex));

                    }
                });



                dataInputStream.close();
                dataOutputStream.close();

                socket.close();
            } catch (UnknownHostException e){e.printStackTrace();}catch (IOException e){e.printStackTrace();}
            return null;
        }
    }

}
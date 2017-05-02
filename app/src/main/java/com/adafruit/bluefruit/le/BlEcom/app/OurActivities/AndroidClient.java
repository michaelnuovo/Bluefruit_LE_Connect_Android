package com.adafruit.bluefruit.le.BlEcom.app.OurActivities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.adafruit.bluefruit.le.BlEcom.R;
import com.adafruit.bluefruit.le.BlEcom.app.OurActivities.PacketWrappers.Palette1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AndroidClient extends Activity {

    static EditText textOut;
    static TextView textIn;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_client);
        textOut = (EditText)findViewById(R.id.textout);
        Button buttonSend = (Button)findViewById(R.id.send);
        textIn = (TextView)findViewById(R.id.textin);
        buttonSend.setOnClickListener(buttonSendOnClickListener);
    }

    // Click listener performs networking sub-routines on a new thread
    Button.OnClickListener buttonSendOnClickListener
            = new Button.OnClickListener(){
        @Override
        public void onClick(View arg0) {
            newThread();
        }
    };

    // Networking subroutines are performed here
    private static void newThread(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    //Your code goes here

                    // TODO Auto-generated method stub
                    Socket socket = null;
                    DataOutputStream dataOutputStream = null;
                    DataInputStream dataInputStream = null;

                    try {
                        socket = new Socket("192.168.1.101", 8888);

                        dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        dataInputStream = new DataInputStream(socket.getInputStream());

                        //dataOutputStream.writeUTF(textOut.getText().toString());
                        int color = Color.rgb(0xFF,0x00,0x00);
                        byte[] packet = new Palette1(color).packet;
                        dataOutputStream.write(packet, 0, packet.length);

                        textIn.setText(dataInputStream.readUTF());
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    finally{
                        if (socket != null){
                            try {
                                socket.close();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        if (dataOutputStream != null){
                            try {
                                dataOutputStream.close();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        if (dataInputStream != null){
                            try {
                                dataInputStream.close();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}
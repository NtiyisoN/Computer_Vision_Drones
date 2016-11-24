package com.zachary_moore.dji_gesture_control;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

import dji.sdk.mobilerc.DJIMobileRemoteController;


public class MainActivity extends AppCompatActivity {

    private DJIMobileRemoteController _masterControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        _masterControl = new DJIMobileRemoteController();


        TextView t = (TextView) this.findViewById(R.id.commandList);
        t.setMovementMethod(new ScrollingMovementMethod());

        this.findViewById(R.id.sendCommand).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseAndCommand();
            }
        });

        this.findViewById(R.id.capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureGestures();
            }
        });


    }

    public void parseAndCommand() {

        TextView commands = (TextView) findViewById(R.id.commandList);

        String comList = commands.getText().toString();
        BufferedReader br = new BufferedReader(new StringReader(comList));

        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.equals("Up")) {
                    _masterControl.setLeftStickVertical((float) .25);
                    TimeUnit.SECONDS.sleep(1);
                    _masterControl.setLeftStickVertical((float) 0);
                }
                else{
                    commands.setText("Something Went Wrong");
                }
            }
        } catch (Exception ie) {
            ie.printStackTrace();

        }


    }

    public void captureGestures(){

        //how do we open a camera this is insane

    }

}

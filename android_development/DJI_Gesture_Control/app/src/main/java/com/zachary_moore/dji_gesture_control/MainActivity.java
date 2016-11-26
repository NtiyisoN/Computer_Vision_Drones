package com.zachary_moore.dji_gesture_control;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import java.util.List;

import dji.sdk.mobilerc.DJIMobileRemoteController;
import android.hardware.Camera;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private DJIMobileRemoteController _masterControl;
    Camera c;
    SurfaceView pv;

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

        System.out.println("Number of cameras: " + Camera.getNumberOfCameras());

        try {
            c = Camera.open(1); // attempt to get a Camera instance
            System.out.println("Successfully opened camera!");
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            System.out.println("Failed to open camera!");
        }


        SurfaceView pv = (SurfaceView) findViewById(R.id.videoFeed);
        SurfaceHolder surfaceHolder = pv.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        pv = (SurfaceView) findViewById(R.id.videoFeed);

        System.out.println("camera: " + c);
        System.out.println("preview: " + pv);

        try {
            c.setPreviewDisplay(pv.getHolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {


        Camera.Parameters params = c.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        Camera.Size selected = sizes.get(0);
        params.setPreviewSize(selected.width,selected.height);
        c.setParameters(params);

        c.setDisplayOrientation(270);
        c.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

}

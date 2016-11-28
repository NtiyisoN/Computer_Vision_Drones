package com.zachary_moore.dji_gesture_control;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import java.util.List;

import dji.sdk.mobilerc.DJIMobileRemoteController;

import android.hardware.Camera;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private DJIMobileRemoteController _masterControl;

    //opencv stuff
    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;

    //orientation stuff
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;

    static{System.loadLibrary("opencv_java3");}

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    Camera c;
    SurfaceView pv;


    public MainActivity() {
        Log.i(TAG, "Instantiated new" + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        _masterControl = new DJIMobileRemoteController();
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.main_camera);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);


    }
/*
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
                } else {
                    commands.setText("Something Went Wrong");
                }
            }
        } catch (Exception ie) {
            ie.printStackTrace();

        }


    }
*/


    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        mRgba = inputFrame.gray();

        //Core.transpose(mRgba, mRgbaT);
        //Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(),0,0,0);
        //Core.flip(mRgbaF, mRgba, 1);
        Imgproc i = new Imgproc();
        i.threshold(mRgba,mRgba, 70, 255, i.THRESH_BINARY);
        //i.adaptiveThreshold(mRgba,mRgba, 255, i.ADAPTIVE_THRESH_MEAN_C, i.THRESH_BINARY_INV,11,2);


        return mRgba;
    }
    public void onCameraViewStarted(int width, int height){
        mRgba = new Mat(height, width, CvType.CV_8UC4);

        //mRgbaF = new Mat(height,width, CvType.CV_8UC4);
        //mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped(){
        mRgba.release();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "internal opencv lib not found");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,this,mLoaderCallback);
        }
        else{
            Log.d(TAG, "lib found and using");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }

}

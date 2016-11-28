package com.zachary_moore.dji_gesture_control;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import java.util.List;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.mobilerc.DJIMobileRemoteController;
import dji.sdk.sdkmanager.DJISDKManager;

import android.hardware.Camera;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.BackgroundSubtractorKNN;
import org.opencv.video.BackgroundSubtractorMOG2;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private DJIMobileRemoteController _masterControl;

    //opencv stuff
    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private BackgroundSubtractorMOG2 mBG;

    //orientation stuff
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;
/*
    //DJI stuff
    private DJIBaseProduct mProduct;
    private DJISDKManager.DJISDKManagerCallback mDJISDKManagerCallback = new DJISDKManager.DJISDKManagerCallback(){
        @Override
        public void onGetRegisteredResult(DJIError error){
            Log.d(TAG, error==null? "Success" : error.getDescription());
            if(error == DJISDKError.REGISTRATION_SUCCESS){
                DJISDKManager.getInstance().startConnectionToProduct();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
                    }
                });
            }
            else{
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(getApplicationContext(), "fail register", Toast.LENGTH_LONG).show();
                    }
                });
            }
            Log.e("TAG", error.toString());
        }
        @Override
        public void onProductChanged(DJIBaseProduct oldProduct, DJIBaseProduct newProduct){
            mProduct = newProduct;
            if(mProduct != null){
                //mProduct.setDJIBaseProductListener(mDJIBaseProductListener);
            }
            //notifyStatusChange();
        }
    };
    */
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

    //Camera c;
    //SurfaceView pv;


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

        //DJISDKManager.getInstance().initSDKManager(this, mDJISDKManagerCallback);


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


    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.gray();
        Mat rep2 = new Mat();
        Mat rep3 = inputFrame.gray();
        Mat contours = new Mat();
        List<MatOfPoint> cont = new ArrayList<MatOfPoint>();
        Mat finalCon = new Mat();

        Imgproc i = new Imgproc();

        i.GaussianBlur(mRgba, mRgba, new Size(5, 5), 0, 0, 0);
        double thresh = i.threshold(mRgba, rep2, 45, 255, i.THRESH_BINARY_INV);

        //return rep2;

        i.erode(rep2, rep2, new Mat(), new Point(-1,-1), 2);
        i.dilate(rep2, rep2, new Mat(), new Point(-1,-1), 2);

        Scalar color = new Scalar(255, 0,255);


        i.findContours(rep2, cont, contours, i.RETR_EXTERNAL, i.CHAIN_APPROX_SIMPLE);
        int maxCont = 0;
        for(int h = 0; h < cont.size(); h ++){
            if(i.contourArea(cont.get(h)) > i.contourArea(cont.get(maxCont))){
                maxCont = h;
            }
        }
        /*
        for(int  q = 0; q < cont.size(); q++) {
            i.drawContours(mRgba, cont, q, color, 2, 15, new Mat(), 0, new Point(0, 0));
        }
        */
        i.drawContours(mRgba, cont, maxCont, color, 2, 15, new Mat(), 0, new Point(0, 0));

        MatOfPoint hullPointMat = new MatOfPoint();
        MatOfInt hull = new MatOfInt();
        i.convexHull(cont.get(maxCont), hull, false);
        MatOfInt4 defects = new MatOfInt4();
        i.convexityDefects(cont.get(maxCont), hull, defects);




        int start = (int)defects.get(0,0)[0];
        Point pStart= new Point(cont.get(maxCont).get(start,0));
        int end = (int)defects.get(1,0)[0];
        Point pEnd = new Point(cont.get(maxCont).get(end,0));
        int far = (int)defects.get(2,0)[0];
        Point pFar = new Point(cont.get(maxCont).get(far,0));

        i.line(mRgba, pStart, pEnd, color, 15);
        i.line(mRgba, pStart, pFar, color, 15);
        i.line(mRgba, pEnd, pFar, color, 15);
        i.circle(mRgba, pFar, 4, color, 15);






        //i.drawContours(mRgba, single, 0, color, 2, 15, new Mat(), 0, new Point(0,0));


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

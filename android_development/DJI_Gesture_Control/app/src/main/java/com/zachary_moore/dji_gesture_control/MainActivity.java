package com.zachary_moore.dji_gesture_control;


import android.app.Activity;
import android.app.Application;
import android.app.ApplicationErrorReport;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketImplFactory;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.DJIVirtualStickFlightControlData;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.products.DJIAircraft;
import dji.sdk.sdkmanager.DJISDKManager;



public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    //opencv stuff
    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;
    public static final String FLAG_CONNECTION_CHANGE = "SOMETHING CHANGED";
    Mat mRgba;
    private int mSubsequent = 0;
    private int mCommand = 0;
    private DJIBaseProduct mProduct;
    private DJIFlightController mcontrol;

    private boolean mFlightCommandReady = false;

    static {
        System.loadLibrary("opencv_java3");
    }

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


    public MainActivity() {Log.i(TAG, "Instantiated new" + this.getClass());}

    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.INTERNET,android.Manifest.permission.ACCESS_WIFI_STATE,android.Manifest.permission.WAKE_LOCK,android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_NETWORK_STATE,android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.CHANGE_WIFI_STATE, android.Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.SYSTEM_ALERT_WINDOW,android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN},1);
        getSupportActionBar().hide();

        /*try{
            SSLContext sslcontext = SSLContext.getInstance("TLSv1");
            sslcontext.init(null,null,null);
            SSLSocketFactory NoSSLV3Factory = new NoSSLv3SocketFactory(sslcontext.getSocketFactory());



            HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLV3Factory);

            Log.e("TAG", "GOOD");

        }
        catch(Exception e){
            Log.e("TAG","bad");
            showToast(e.toString());
        }*/

        mHandler = new Handler(Looper.getMainLooper());
        DJISDKManager.getInstance().initSDKManager(this, mDJISDKManagerCallback);





        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.main_camera);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);


    }


    private DJISDKManager.DJISDKManagerCallback mDJISDKManagerCallback = new DJISDKManager.DJISDKManagerCallback() {
        @Override
        public void onGetRegisteredResult(DJIError error) {
            Log.d(TAG, error == null ? "success" : error.getDescription());
            if(error == DJISDKError.REGISTRATION_SUCCESS) {
                DJISDKManager.getInstance().startConnectionToProduct();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Register App Successful", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Register App Failed! Please enter your App Key and check the network.", Toast.LENGTH_LONG).show();
                    }
                });
            }
            Log.e("TAG", error.getDescription());
        }

        @Override
        public void onProductChanged(DJIBaseProduct oldProduct, DJIBaseProduct newProduct) {
            mProduct = newProduct;
            if(mProduct != null) {
                mProduct.setDJIBaseProductListener(mDJIBaseProductListener);

                mFlightCommandReady = true;

                DJIBaseProduct prod = DJISDKManager.getInstance().getDJIProduct();
                mcontrol = ((DJIAircraft) prod).getFlightController();
                mcontrol.enableVirtualStickControlMode(new DJICommonCallbacks.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {

                    }
                });
                mcontrol.takeOff(new DJICommonCallbacks.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {

                    }
                });

            }
            notifyStatusChange();
        }
    };


    private DJIBaseProduct.DJIBaseProductListener mDJIBaseProductListener = new DJIBaseProduct.DJIBaseProductListener() {
        @Override
        public void onComponentChange(DJIBaseProduct.DJIComponentKey key, DJIBaseComponent oldComponent, DJIBaseComponent newComponent) {
            if(newComponent != null) {
                newComponent.setDJIComponentListener(mDJIComponentListener);
            }
            notifyStatusChange();
        }
        @Override
        public void onProductConnectivityChanged(boolean isConnected) {
            notifyStatusChange();
        }
    };
    private DJIBaseComponent.DJIComponentListener mDJIComponentListener = new DJIBaseComponent.DJIComponentListener() {
        @Override
        public void onComponentConnectivityChanged(boolean isConnected) {
            notifyStatusChange();
        }
    };
    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };


    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

//        Log.e("TAG", "mProduct connected: " + mProduct.isConnected());

        mRgba = inputFrame.gray();

        Mat rep2 = new Mat();
        Mat contours = new Mat();
        List<MatOfPoint> cont = new ArrayList<MatOfPoint>();

        Imgproc i = new Imgproc();

        i.GaussianBlur(mRgba, mRgba, new Size(5, 5), 0, 0, 0);
        i.threshold(mRgba, rep2, 35, 255, i.THRESH_BINARY_INV);


        i.erode(rep2, rep2, new Mat(), new Point(-1, -1), 2);
        i.dilate(rep2, rep2, new Mat(), new Point(-1, -1), 2);

        Scalar color = new Scalar(255, 0, 255);

        i.findContours(rep2, cont, contours, i.RETR_EXTERNAL, i.CHAIN_APPROX_SIMPLE);
        if (cont.size() <= 0) {
            return inputFrame.gray();
        }
        int maxCont = 0;
        for (int h = 0; h < cont.size(); h++) {
            if (i.contourArea(cont.get(h)) > i.contourArea(cont.get(maxCont))) {
                maxCont = h;
            }
        }

        i.drawContours(mRgba, cont, maxCont, color, 2, 15, new Mat(), 0, new Point(0, 0));

        //hull indices of points in cont.get(maxCont) max contour
        MatOfInt hull = new MatOfInt();
        i.convexHull(cont.get(maxCont), hull, false);

        MatOfInt4 defects = new MatOfInt4();
        i.convexityDefects(cont.get(maxCont), hull, defects);

        int command = 0;

        for (int y = 1; y < defects.rows(); y++) {

            if ((double) defects.get(y, 0)[3] / 256 > 120) {

                int start = (int) defects.get(y, 0)[0];
                Point pStart = new Point(cont.get(maxCont).get(start, 0));
                int end = (int) defects.get(y, 0)[1];
                Point pEnd = new Point(cont.get(maxCont).get(end, 0));
                int far = (int) defects.get(y, 0)[2];
                Point pFar = new Point(cont.get(maxCont).get(far, 0));

                //i.line(mRgba, pStart, pEnd, color, 5);
                //i.line(mRgba, pStart, pFar, color, 5);
                //i.line(mRgba, pEnd, pFar, color, 5);
                command+=1;
                i.circle(mRgba, pEnd, 4, color, 15);

            }

        }

        int frameDelay = 5;

        if (mFlightCommandReady) {

            switch (command) {
                case 0:
                    if (mCommand == 0) {
                        mSubsequent += 1;
                        if (mSubsequent == frameDelay) {
                            showToast("down");
//
//                            mcontrol.sendVirtualStickFlightControlData(new DJIVirtualStickFlightControlData(0, 1, 0, 0), new DJICommonCallbacks.DJICompletionCallback() {
//                                @Override
//                                public void onResult(DJIError djiError) {
//
//                                }
//                            });
                            mSubsequent = 0;
                        }
                    } else {
                        mSubsequent = 1;
                        mCommand = 0;
                    }
                    //Stop

                    //showToast((Integer.toString(command)));

                    break;


                case 1:
                    //Down
                    //showToast("down");
                    //showToast((Integer.toString(command)));
                    if (mCommand == 1) {
                        mSubsequent += 1;
                        if (mSubsequent == frameDelay) {

                            showToast("stop");

                            mcontrol.autoLanding(new DJICommonCallbacks.DJICompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {

                                }
                            });
                            mSubsequent = 0;
                        }
                    } else {
                        mSubsequent = 1;
                        mCommand = 1;
                    }
                    break;

                case 2:
                    //left
                    //showToast("left");
                    //showToast((Integer.toString(command)));
                    if (mCommand == 2) {
                        mSubsequent += 1;
                        if (mSubsequent == frameDelay) {
//                            mcontrol.sendVirtualStickFlightControlData(new DJIVirtualStickFlightControlData(0, 1, 0, 0), new DJICommonCallbacks.DJICompletionCallback() {
//                                @Override
//                                public void onResult(DJIError djiError) {
//
//                                }
//                            });
                            showToast("left");
                            mSubsequent = 0;
                        }
                    } else {
                        mSubsequent = 1;
                        mCommand = 2;
                    }
                    break;

                case 3:
                    //right
                    //showToast("right");
                    //showToast((Integer.toString(command)));
                    if (mCommand == 3) {
                        mSubsequent += 1;
                        if (mSubsequent == frameDelay) {
                            showToast("right");
//                            mcontrol.sendVirtualStickFlightControlData(new DJIVirtualStickFlightControlData(0, 1, 0, 0), new DJICommonCallbacks.DJICompletionCallback() {
//                                @Override
//                                public void onResult(DJIError djiError) {
//
//                                }
//                            });
                            mSubsequent = 0;
                        }
                    } else {
                        mSubsequent = 1;
                        mCommand = 3;
                    }
                    break;

                case 4:
                    //UP
                    //showToast("up");
                    //showToast((Integer.toString(command)));
                    if (mCommand == 4) {
                        mSubsequent += 1;
                        if (mSubsequent == frameDelay) {
                            showToast("Take Off");
                            mcontrol.takeOff(new DJICommonCallbacks.DJICompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {

                                }
                            });
//                            mcontrol.sendVirtualStickFlightControlData(new DJIVirtualStickFlightControlData(0, 1, 0, 0), new DJICommonCallbacks.DJICompletionCallback() {
//                                @Override
//                                public void onResult(DJIError djiError) {
//
//                                }
//                            });
                            mSubsequent = 0;
                        }
                    } else {
                        mSubsequent = 1;
                        mCommand = 4;
                    }
                    break;
            }
        }

        return mRgba;

    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            //Log.d(TAG, "internal opencv lib not found");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            //Log.d(TAG, "lib found and using");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

}

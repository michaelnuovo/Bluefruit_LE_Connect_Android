package com.adafruit.bluefruit.le.BLEcom.app.update;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class LooperThread extends Thread {

    // Class variables
    private static LooperThread mLooperThread;

    // Instance variables
    public Handler mHandler;

    public static LooperThread getInstance(){
        if(mLooperThread != null)
            return mLooperThread;
        mLooperThread = new LooperThread();
        return mLooperThread;
    }

    private LooperThread(){  }

    @Override
    public void run(){
        Log.v("TAG","Running");
        if (Looper.myLooper() == null) Looper.prepare();
        mHandler = new Handler();
        Looper.loop();
    }
}

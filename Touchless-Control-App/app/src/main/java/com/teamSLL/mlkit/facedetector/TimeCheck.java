package com.teamSLL.mlkit.facedetector;

import android.util.Log;

public class TimeCheck {
    private long threshold;
    private long prevTime;

    public TimeCheck(long threshold){
        this.threshold = threshold;
        this.prevTime = 0;
    }
    public boolean isOveredThreshold(){
        if(prevTime == 0){
            prevTime = System.currentTimeMillis();
            return false;
        }
        if(System.currentTimeMillis() - prevTime < threshold) return false;

        prevTime = System.currentTimeMillis();
        return true;
    }
    public void setPrevTime(long time){
        this.prevTime = time;
    }
}

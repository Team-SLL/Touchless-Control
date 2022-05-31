package com.teamSLL.mlkit.facedetector;

import android.util.Log;

import com.google.mlkit.vision.face.Face;

public class HeadRecognition {
    private double x_center, y_center;
    private double x_threshold, y_threshold;
    private TimeCheck udChecker, rlChecker;

    // y is left/right, x is up/down
    public HeadRecognition(double ud_center, double rl_center, double ud_threshold, double rl_threshold, long ms){
        this.x_center = ud_center;
        this.y_center = rl_center;
        this.x_threshold = ud_threshold;
        this.y_threshold = rl_threshold;

        this.udChecker = new TimeCheck(ms);
        this.rlChecker = new TimeCheck(ms);
    }

    public int generationUD(Face face){
        double x = face.getHeadEulerAngleX();

        if(x < x_center - x_threshold) {
            if(udChecker.isOveredThreshold()){
                return -1;
            }
        }
        else if(x > x_center + x_threshold) {
            if(udChecker.isOveredThreshold()){
               return 1;
            }
        }else{
            udChecker.setPrevTime(0);
        }
        return 0;
    }
    public int generationRL(Face face){
        double y = face.getHeadEulerAngleY();

        if(y < y_center - y_threshold) {
            if(rlChecker.isOveredThreshold()) {
                return -1;
            }
        }
        else if(y > y_center + y_threshold) {
            if(rlChecker.isOveredThreshold()){
                return 1;
            }
        }else{
            rlChecker.setPrevTime(0);
        }
        return 0;
    }
}

package com.teamSLL.mlkit.facedetector;

import com.google.mlkit.vision.face.Face;

public class EyeRecognition {
    private TimeCheck shortChecker, longChecker;

    //1s = 3times
    public EyeRecognition(long shortMs, long longMs){
        shortChecker = new TimeCheck(shortMs);
        longChecker = new TimeCheck(longMs);
    }

    public int generationClosed(Face face){
        if(face.getLeftEyeOpenProbability() == null || face.getRightEyeOpenProbability() == null) return 0;

        if(face.getLeftEyeOpenProbability() < 0.1 && face.getRightEyeOpenProbability() < 0.1){
            if(longChecker.isOveredThreshold()){
                return -1;
            }
            if(shortChecker.isOveredThreshold()){
                return 1;
            }
        }else{
            shortChecker.setPrevTime(0);
            longChecker.setPrevTime(0);
        }
        return 0;
    }
    public void setTimeChecker(int ms){
        this.shortChecker = new TimeCheck(ms);
    }
}

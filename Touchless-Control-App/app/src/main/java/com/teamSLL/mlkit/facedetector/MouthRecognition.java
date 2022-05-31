package com.teamSLL.mlkit.facedetector;

import static com.google.mlkit.vision.face.FaceContour.LOWER_LIP_TOP;
import static com.google.mlkit.vision.face.FaceContour.UPPER_LIP_BOTTOM;

import android.graphics.PointF;

import com.google.mlkit.vision.face.Face;

import java.sql.Time;
import java.util.List;

public class MouthRecognition {
    private double threshold;
    private TimeCheck checker;

    public MouthRecognition(double threshold, long ms){
        this.threshold = threshold;
        this.checker = new TimeCheck(ms);
    }

    public int generationOpen(Face face){
        if(face.getContour(LOWER_LIP_TOP) == null || face.getContour(UPPER_LIP_BOTTOM) == null) return 0;

        double sum = 0;
        List<PointF> top = face.getContour(LOWER_LIP_TOP).getPoints();
        List<PointF> bottom = face.getContour(UPPER_LIP_BOTTOM).getPoints();

        for(int i=0;i<9;i++){
            sum += top.get(i).y - bottom.get(8 - i).y;
        }
        if(sum > threshold){
            if(checker.isOveredThreshold()){
                return 1;
            }
        }else{
            checker.setPrevTime(0);
        }

        return 0;
    }

}

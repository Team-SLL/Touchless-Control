/*
* 눈, 얼굴, 입 인식 클래스를 모아서 한번에 출력하기 위한 클래스
*
* */

package com.teamSLL.mlkit.facedetector;

import android.util.Log;

import com.google.mlkit.vision.face.Face;

import java.util.Arrays;
import java.util.Collections;

public class MotionRecongition {
    private HeadRecognition hr;
    private EyeRecognition er;
    private MouthRecognition mr;

    public static final int MOTION_DEFAULT = 0x0;
//    public static final int HEAD_UP = 0x0000001;
//    public static final int HEAD_DOWN = 0x0000010;
//    public static final int HEAD_LEFT = 0x0000100;
//    public static final int HEAD_RIGHT = 0x0001000;
//    public static final int MOUTH_OPEN = 0x0010000;
//    public static final int EYE_CLOSED_SHORT = 0x0100000;
//    public static final int EYE_CLOSED_LONG = 0x1000000;
    public static final int HEAD_UP = 1;
    public static final int HEAD_DOWN = 2;
    public static final int HEAD_LEFT = 3;
    public static final int HEAD_RIGHT = 4;
    public static final int MOUTH_OPEN = 5;
    public static final int EYE_CLOSED_SHORT = 6;
    public static final int EYE_CLOSED_LONG = 7;

    public MotionRecongition(){
        hr = new HeadRecognition(0,0,10,15, 300);
        er = new EyeRecognition(1000, 1000000);
        mr = new MouthRecognition(40,1000);
    }

    public int getHeadRLEvent(Face face){
        int result = hr.generationRL(face);
        switch(result){
            case -1:
                return HEAD_RIGHT;
            case 1:
                return HEAD_LEFT;
            default:
                return MOTION_DEFAULT;
        }
    }
    public int getHeadUDEvent(Face face){
        int result = hr.generationUD(face);
        switch(result){
            case -1:
                return HEAD_DOWN;
            case 1:
                return HEAD_UP;
            default:
                return MOTION_DEFAULT;
        }
    }
    public int getEyeClosedEvent(Face face){
        int result = er.generationClosed(face);
        switch(result){
            case -1:
                return EYE_CLOSED_LONG;
            case 1:
                return EYE_CLOSED_SHORT;
            default:
                return MOTION_DEFAULT;
        }
    }
    public int getMouseOpenEvent(Face face){
        int result = mr.generationOpen(face);
        switch(result){
            case 1:
                return MOUTH_OPEN;
            default:
                return MOTION_DEFAULT;
        }
    }

    public int getAllEvent(Face face){

        return (int) Collections.max(Arrays.asList(
                getHeadRLEvent(face),
                getHeadUDEvent(face),
                getEyeClosedEvent(face),
                getMouseOpenEvent(face)));
    }
}

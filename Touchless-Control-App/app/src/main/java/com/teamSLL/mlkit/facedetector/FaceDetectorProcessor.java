/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
* FaceDetectorProcessor
* 얼굴 검출 및 랜드마크 검출하는 Face Detector 생성하는 클래스
* */

package com.teamSLL.mlkit.facedetector;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import com.teamSLL.mlkit.VisionProcessorBase;
import com.teamSLL.mlkit.camera.GraphicOverlay;

import java.util.List;
import java.util.Locale;

/**
 * Detector 생성. 즉 실질적으로 얼굴인식을 해주는 클래스
 * 입력은 imageProcessor.processImageProxy(imageProxy, graphicOverlay)를 통해 이미지가 입력되어 분석함
 * processImageProxy 함수는 VisionProcessorBase에 존재
 * 얼굴 인식 성공하면 onSucess()를 통해서 Face함수를 저장함
 * 메인 액티비티에서 getFace를 통해 얼굴 정보(Face)를 가져옴
 * */

public class FaceDetectorProcessor extends VisionProcessorBase<List<Face>> {

  private static final String TAG = "FaceDetectorProcessor";

  private final FaceDetector detector;
  private List<Face> Faces;
  private MotionRecongition MR;


  public FaceDetectorProcessor(Context context) {
    super(context);
    FaceDetectorOptions highAccuracyOpts =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                    .build();
    // detector생성
    detector = FaceDetection.getClient(highAccuracyOpts);
    MR = new MotionRecongition();

//    FaceDetectorOptions faceDetectorOptions = PreferenceUtils.getFaceDetectorOptions(context);
//    Log.v(MANUAL_TESTING_LOG, "Face detector options: " + faceDetectorOptions);
//    detector = FaceDetection.getClient(faceDetectorOptions);
  }

  @Override
  public void stop() {
    super.stop();
    detector.close();
  }

  @Override
  protected Task<List<Face>> detectInImage(InputImage image) {
    // 얼굴 분석 실행
    //mlkit의 detector 자체 함수
    return detector.process(image);
  }

  @Override
  // 얼굴 검출에 성공하면 랜드마크를 graphicOverlay에 저장
  protected void onSuccess(@NonNull List<Face> faces, @NonNull GraphicOverlay graphicOverlay) {
    Faces = faces;
    for (Face face : faces) {
      graphicOverlay.add(new FaceGraphic(graphicOverlay, face)); // 컨투어 등 이미지 그려주는거라 없어도 작동함.
    //  logExtrasForTesting(face);
    }
  }
//  @Override
//  // 얼굴 검출에 성공하면 랜드마크를 graphicOverlay에 저장
//  protected void onSuccess(@NonNull List<Face> faces) {
//    Faces = faces;
//  }

  public int getFaceMoveEvent(){
    if(this.Faces == null || this.Faces.isEmpty()){
      Log.i("event", "Cant Find Face");
      return 0;
    }
    return MR.getAllEvent(this.Faces.get(0));
  }

  // 검출된 얼굴들에 대한 로그 테스트
  private static void logExtrasForTesting(Face face) {
    if (face != null) {
      Log.v(MANUAL_TESTING_LOG, "face bounding box: " + face.getBoundingBox().flattenToString());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle X: " + face.getHeadEulerAngleX());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle Y: " + face.getHeadEulerAngleY());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle Z: " + face.getHeadEulerAngleZ());

      // All landmarks
      int[] landMarkTypes =
          new int[] {
            FaceLandmark.MOUTH_BOTTOM,
            FaceLandmark.MOUTH_RIGHT,
            FaceLandmark.MOUTH_LEFT,
            FaceLandmark.RIGHT_EYE,
            FaceLandmark.LEFT_EYE,
            FaceLandmark.RIGHT_EAR,
            FaceLandmark.LEFT_EAR,
            FaceLandmark.RIGHT_CHEEK,
            FaceLandmark.LEFT_CHEEK,
            FaceLandmark.NOSE_BASE
          };
      String[] landMarkTypesStrings =
          new String[] {
            "MOUTH_BOTTOM",
            "MOUTH_RIGHT",
            "MOUTH_LEFT",
            "RIGHT_EYE",
            "LEFT_EYE",
            "RIGHT_EAR",
            "LEFT_EAR",
            "RIGHT_CHEEK",
            "LEFT_CHEEK",
            "NOSE_BASE"
          };
      for (int i = 0; i < landMarkTypes.length; i++) {
        FaceLandmark landmark = face.getLandmark(landMarkTypes[i]);
        if (landmark == null) {
          Log.v(
              MANUAL_TESTING_LOG,
              "No landmark of type: " + landMarkTypesStrings[i] + " has been detected");
        } else {
          PointF landmarkPosition = landmark.getPosition();
          String landmarkPositionStr =
              String.format(Locale.US, "x: %f , y: %f", landmarkPosition.x, landmarkPosition.y);
          Log.v(
              MANUAL_TESTING_LOG,
              "Position for face landmark: "
                  + landMarkTypesStrings[i]
                  + " is :"
                  + landmarkPositionStr);
        }
      }
      Log.v(
          MANUAL_TESTING_LOG,
          "face left eye open probability: " + face.getLeftEyeOpenProbability());
      Log.v(
          MANUAL_TESTING_LOG,
          "face right eye open probability: " + face.getRightEyeOpenProbability());
      Log.v(MANUAL_TESTING_LOG, "face smiling probability: " + face.getSmilingProbability());
      Log.v(MANUAL_TESTING_LOG, "face tracking id: " + face.getTrackingId());
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Face detection failed " + e);
  }
}

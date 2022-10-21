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
 * 메인 액티비티, 실시간 감지 - UI 등등
 * */
package com.teamSLL.mlkit;

import android.Manifest;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.util.Size;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;

import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.common.MlKitException;
import com.teamSLL.mlkit.STT.SpeechToText;
import com.teamSLL.mlkit.screen.Setting;
import com.teamSLL.mlkit.screen.UI;
import com.teamSLL.mlkit.facedetector.FaceDetectorProcessor;
import com.teamSLL.mlkit.preference.PreferenceUtils;
import com.teamSLL.mlkit.camera.CameraXViewModel;
import com.teamSLL.mlkit.camera.GraphicOverlay;
import com.teamSLL.mlkit.camera.VisionImageProcessor;

import java.util.ArrayList;

/*
 * initUI : youtube UI 관련 초기화 함수
 * onCreate : 생성, 초기화
 * bindAnalysisUseCase : 반복하며, 얼굴 움직임을 읽는 함수
 * event : 얼굴 분석한 결과에 따라 움직임을 판별해 실제 액션을 하는 함수
 * */
@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
public final class MainActivity extends AppCompatActivity {
  private static final String TAG = "CameraXLivePreview";
  private static final String FACE_DETECTION = "Face Detection";
  private static final String STATE_SELECTED_MODEL = "selected_model";

  @Nullable private ProcessCameraProvider cameraProvider;
  @Nullable private ImageAnalysis analysisUseCase;
  @Nullable private VisionImageProcessor imageProcessor;
  private boolean needUpdateGraphicOverlayImageSourceInfo;


  private String selectedModel = FACE_DETECTION;
  private int lensFacing = CameraSelector.LENS_FACING_BACK; // mobile - LENS_FACING_FRONT, pc - LENS_FACING_BACK

  private CameraSelector cameraSelector;
  private PermissionSupport permission;
  private Setting setting;

  private UI ui; // UI를 조작하는데 관여하는 클래스

  private SpeechToText stt;  // 음성명령어 조작을 위해 작성

  private GraphicOverlay graphicOverlay; // 왼쪽 하단의 얼굴에 레이아웃 씌운것
  private TextView tv; // 얼굴 움직임을 0~7로 표시.

  private ArrayList<ArrayList<Short>> motionToUI = new ArrayList<>(); // 얼굴 움직임 - UI 조작을 array list 에 넣어서 관리


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);

    if (savedInstanceState != null) {
      selectedModel = savedInstanceState.getString(STATE_SELECTED_MODEL, FACE_DETECTION);
    }

    permissionCheck();

    setting = new Setting(this, getApplicationContext());

    initUI();
  }



  private void initCamera(){
    cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
    new ViewModelProvider(this, AndroidViewModelFactory.getInstance(getApplication()))
            .get(CameraXViewModel.class)
            .getProcessCameraProvider()
            .observe(
                    this,
                    provider -> {
                      cameraProvider = provider;
                      bindAnalysisUseCase();
                    });
  }

  private void initMic(){

  }

  private void permissionCheck() {
    permission = new PermissionSupport(this, this);

    if (!permission.checkPermission()){
      permission.requestPermission();
    }

    if(Build.VERSION.SDK_INT >= VERSION_CODES.M) {
      if(permission.checkPermission(Manifest.permission.RECORD_AUDIO)){
        initMic();
      }
      if (permission.checkPermission(Manifest.permission.CAMERA)) {
        initCamera();
      }
    }
  }
  // Request Permission에 대한 결과 값 받아와
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    //여기서도 리턴이 false로 들어온다면 (사용자가 권한 허용 거부)
    if (!permission.permissionResult(requestCode, permissions, grantResults)) {
      // 다시 permission 요청
      permission.requestPermission();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    initCamera();
    initMic();
  }


  // layout Init
  private void initUI() {

    tv = findViewById(R.id.textView); //현재 얼굴인식 결과 : 테스트용
    graphicOverlay = findViewById(R.id.graphic_overlay);

    // UI를 전반적으로 제어하는 클래스 생성
    ui = new UI(this, getApplicationContext());
    // stt로 음성 명령어를 실행시키기 위해 작성해볼게요
    stt = new SpeechToText(getApplicationContext(), ui); //음성인식 활성화
  }


  @Override
  protected void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putString(STATE_SELECTED_MODEL, selectedModel);
  }

  @Override
  public void onResume() {
    super.onResume();
    bindAnalysisUseCase();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (imageProcessor != null) {
      imageProcessor.stop();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (imageProcessor != null) {
      imageProcessor.stop();
    }
  }

  private void bindAnalysisUseCase() {
    if (cameraProvider == null) {
      return;
    }
    if (analysisUseCase != null) {
      cameraProvider.unbind(analysisUseCase);
    }
    if (imageProcessor != null) {
      imageProcessor.stop();
    }
    Log.i(TAG, "Using Face Detector Processor");

    // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
    cameraProvider.unbindAll();
    // 얼굴 인식 객체 생성 (모델 생성)
    imageProcessor = new FaceDetectorProcessor(this);
    setHyperparameter();

    ImageAnalysis.Builder builder = new ImageAnalysis.Builder(); //카메라
    Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
    if (targetResolution != null) {
      builder.setTargetResolution(targetResolution);
    }
    analysisUseCase = builder.build();
    needUpdateGraphicOverlayImageSourceInfo = true;
    analysisUseCase.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(this),
            imageProxy -> {

              if (needUpdateGraphicOverlayImageSourceInfo) {
                //boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_BACK;
                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                  graphicOverlay.setImageSourceInfo(
                          imageProxy.getWidth(), imageProxy.getHeight(), true);
                } else {
                  graphicOverlay.setImageSourceInfo(
                          imageProxy.getHeight(), imageProxy.getWidth(), true);
                }
                needUpdateGraphicOverlayImageSourceInfo = false;
              }
              try {
                //이미지 분석
                imageProcessor.processImageProxy(imageProxy, graphicOverlay);

                if(ui.isSettingOpen()) return;

                //분석 결과에 대해 이용
                int result = ((FaceDetectorProcessor)imageProcessor).getFaceMoveEvent();
                // 얼굴 움직임 결과에 따라 UI 조작

                InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);

                int screen = ui.isFullScreen ? 1 : (imm.isFullscreenMode() ? 2 : 0);
                ui.use(motionToUI.get(screen).get(result));
                switch(result){
                  case 1:
                    tv.setText("HEAD_UP");
                    break;
                  case 2:
                    tv.setText("HEAD_DOWN");
                    break;
                  case 3:
                    tv.setText("HEAD_LEFT");
                    break;
                  case 4:
                    tv.setText("HEAD_RIGHT");
                    break;
                  case 5:
                    tv.setText("MOUTH_OPEN");
                    break;
                  case 6:
                    tv.setText("EYE_CLOSED_SHORT");
                    break;
                  case 7:
                    tv.setText("EYE_CLOSED_LONG");
                    break;
                  default:
                    tv.setText("");
                    break;
                }


              } catch (MlKitException e) {
                Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                        .show();
              }
            });
    // 카메라와 생명주기를 연결. 카메라가 죽으면 같이 종료
    cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
  }

  @Override
  public void onBackPressed() { //뒤로가기
    if (ui.isSearchViewOpened()) {
      ui.closeSearchView();
      return;
    }
    if(ui.isFullScreen){
      ui.use(UI.VIDEO_END);
      return;
    }
    if(ui.isSettingOpen()){
      ui.closeSetting();
      setHyperparameter();
      return;
    }
    super.onBackPressed();
  }

  private void setHyperparameter(){
    motionToUI = setting.getSetting();
    int head = setting.getHeadMs();
    int mouth = setting.getMouthMs();
    int eye = setting.getEyeMs();
    ((FaceDetectorProcessor)imageProcessor).setMs(head, mouth, eye);
  }

}

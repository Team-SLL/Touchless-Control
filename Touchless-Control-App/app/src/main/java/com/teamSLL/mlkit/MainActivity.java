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

import static android.Manifest.permission_group.CAMERA;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.annotation.KeepName;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.mlkit.common.MlKitException;
import com.teamSLL.mlkit.facedetector.FaceDetectorProcessor;
import com.teamSLL.mlkit.preference.PreferenceUtils;
import com.teamSLL.mlkit.source.CameraXViewModel;
import com.teamSLL.mlkit.source.GraphicOverlay;
import com.teamSLL.mlkit.source.VisionImageProcessor;
import com.teamSLL.mlkit.youtube.SearchRunnable;

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
  private int lensFacing = CameraSelector.LENS_FACING_BACK;
  private CameraSelector cameraSelector;

  final int PERMISSION_REQUEST_CODE = 100;
  int APIVersion = Build.VERSION.SDK_INT;


  private UI ui; // UI를 조작하는데 관여하는 클래스

  private GraphicOverlay graphicOverlay; // 왼쪽 하단의 얼굴에 레이아웃 씌운것
  private TextView tv; // 얼굴 움직임을 0~7로 표시.

  private ArrayList<ArrayList<Short>> motionToUI = new ArrayList<>(); // 얼굴 움직임 - UI 조작을 array list 에 넣어서 관리


  // 카메라 권한 체크
  private boolean checkCAMERAPermission(){
    int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
    return result == PackageManager.PERMISSION_GRANTED;
  }
  // 카메라 권한 부여 알람창 생성
  private void showMessagePermission(String message, DialogInterface.OnClickListener okListener){
    new AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("허용", okListener)
            .setNegativeButton("거부", null)
            .create()
            .show();
  }
  @Override
  // 카메라 권한 부여
  public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    switch (requestCode) {
      case PERMISSION_REQUEST_CODE:
        if (grantResults.length > 0) {
          boolean cameraAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
          if (cameraAccepted) {
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
          }else{
            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
              if (shouldShowRequestPermissionRationale(CAMERA)) {
                showMessagePermission("권한 허가를 요청합니다",
                        new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
                              requestPermissions(new String[]{CAMERA}, PERMISSION_REQUEST_CODE);
                            }
                          }
                        });
              }
            }
          }
        }
        break;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_vision_camerax_live_preview);

    if (savedInstanceState != null) {
      selectedModel = savedInstanceState.getString(STATE_SELECTED_MODEL, FACE_DETECTION);
    }

    // 카메라 권한 체크 후 권한 요구
    if(APIVersion >= VERSION_CODES.M) {
      if (checkCAMERAPermission()) {
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
      }else{
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
      }
    }
    
    initUI();
  }
  
  // layout Init
  private void initUI() {
    // IF IN MAIN SCREEN
    ArrayList<Short> mainScreen = new ArrayList<>();
    mainScreen.add(UI.NONE);        // NONE - NONE
    mainScreen.add(UI.VIDEO_START); // TOP - VIDEO START
    mainScreen.add(UI.NONE);        // DOWN - NONE
    mainScreen.add(UI.PREV_VIDEO);  // LEFT - PREV VIDEO
    mainScreen.add(UI.NEXT_VIDEO);  // RIGHT - NEXT VIDEO
    mainScreen.add(UI.SEARCH);      // MOUTH OPEN - SEARCH OPEN
    mainScreen.add(UI.NONE);        // EYE CLOSED SHORT - NONE
    mainScreen.add(UI.NONE);        // EYE CLOSED LONG - NONE
    motionToUI.add(mainScreen);

    // IF IN VIDEO PLAYING SCREEN
    ArrayList<Short> videoScreen = new ArrayList<>();
    videoScreen.add(UI.NONE);             // NONE - NONE
    videoScreen.add(UI.NONE);             // TOP - NONE
    videoScreen.add(UI.VIDEO_END);        // DOWN - VIDEO END
    videoScreen.add(UI.NONE);             // LEFT - NONE
    videoScreen.add(UI.NONE);             // RIGHT - NONE
    videoScreen.add(UI.NONE);             // MOUTH OPEN - NONE
    videoScreen.add(UI.VIDEO_PLAY_STOP);  // EYE CLOSED SHORT - VIDEO PLAY OR STOP
    videoScreen.add(UI.NONE);             // EYE CLOSED LONG - NONE
    motionToUI.add(videoScreen);


    SearchView searchView = findViewById(R.id.search_view);
    tv = findViewById(R.id.textView); //현재 얼굴인식 결과 : 테스트용
    graphicOverlay = findViewById(R.id.graphic_overlay);

    // 영상 리스트뷰 셋팅
    RecyclerView recyclerView = findViewById(R.id.rvVideos); // 영상 목록을 담을 recycler view
    recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();

    // 유튜브 영상을 재생할 프레그먼트
    YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_player_fragment); //유튜브 재생 프래그먼트, 유튜브 영상 재생용

    // UI를 전반적으로 제어하는 클래스 생성
    ui = new UI(getApplicationContext(), recyclerView, manager, youTubePlayerFragment, searchView);
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
                boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_BACK;
                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                  graphicOverlay.setImageSourceInfo(
                          imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                } else {
                  graphicOverlay.setImageSourceInfo(
                          imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                }
                needUpdateGraphicOverlayImageSourceInfo = false;
              }
              try {
                //이미지 분석
                imageProcessor.processImageProxy(imageProxy, graphicOverlay);

                //분석 결과에 대해 이용
                int result = ((FaceDetectorProcessor)imageProcessor).getFaceMoveEvent();
                // 얼굴 움직임 결과에 따라 UI 조작
                ui.use(motionToUI.get(ui.isFullScreen?1:0).get(result));
                tv.setText(Integer.toString(result));


              } catch (MlKitException e) {
                Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                        .show();
              }
            });
    // 카메라와 생명주기를 연결. 카메라가 죽으면 같이 종료
    cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
  }

  public void changeMotionToUI(int where, int selected_motion, short change_ui){
    ArrayList<Short> temp = motionToUI.get(where);
    temp.set(selected_motion, change_ui);
    motionToUI.set(where, temp);
    temp.clear();
  }

  @Override
  public void onBackPressed() { //뒤로가기
    if (!ui.isSearchViewIconified()) {
      ui.closeSearchView();
      return;
    }
    if(ui.isFullScreen){
      ui.use(UI.VIDEO_END);
      return;
    }
    super.onBackPressed();
  }
}

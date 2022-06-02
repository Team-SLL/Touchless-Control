package com.teamSLL.mlkit.STT;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.teamSLL.mlkit.screen.UI;

import java.util.ArrayList;
import java.util.Locale;


public class SpeechToText {
    public static final short NONE = 0;
    public static final short VIDEO_START = 1;
    public static final short VIDEO_END = 2;
    public static final short VIDEO_PLAY_STOP = 3;
    public static final short NEXT_VIDEO = 5;
    public static final short PREV_VIDEO = 6;
    public static final short SEARCH_OPEN = 7;
    public static final short SEARCH_CLOSE = 8;

    private Context context;
    private SpeechRecognizer speechRecognizer;
    private SearchView searchView;
    private ImageButton micoffBtn;
    private ImageButton miconBtn;
    int state = 0;

    public SpeechToText(Context context, UI ui){
        this.context = context;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        micoffBtn = ui.getMicoffBtn();
        miconBtn = ui.getMiconBtn();
        searchView = ui.getSearchView();

        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                if (state == 1){  //음성검색
                    searchView.setQuery("", false);
                    searchView.setQueryHint("Listening...");
                }
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                if (miconBtn.getVisibility() == View.VISIBLE){
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
            }

            @Override
            public void onError(int i) {
                speechRecognizer.startListening(speechRecognizerIntent);
            }

            @Override
            public void onResults(Bundle bundle) {
                if (state == 1){
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    searchView.setQuery(data.get(0), true);
                    state = 0;
                }
                else if (miconBtn.getVisibility() == View.VISIBLE){
                    String key = SpeechRecognizer.RESULTS_RECOGNITION;
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String[] rs = new String[data.size()];
                    data.toArray(rs);
                    FuncVoiceOrderCheck(rs[0],ui,speechRecognizerIntent);

                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });


        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state = 1;
                speechRecognizer.startListening(speechRecognizerIntent);
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state = 1;
                speechRecognizer.startListening(speechRecognizerIntent);
            }
        });

        micoffBtn.setOnClickListener(new View.OnClickListener() {  //음성인식 활성화
            @Override
            public void onClick(View v) {
                micoffBtn.setVisibility(View.GONE);
                miconBtn.setVisibility(View.VISIBLE);

                speechRecognizer.startListening(speechRecognizerIntent);

            }
        });

        miconBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                micoffBtn.setVisibility(View.VISIBLE);
                miconBtn.setVisibility(View.GONE);

                speechRecognizer.stopListening();

            }
        });

    }
    private void FuncVoiceOrderCheck(String VoiceMsg, UI ui, Intent speechRecognizerIntent){
        if(VoiceMsg.length()<1)return;

        VoiceMsg=VoiceMsg.replace(" ","");//공백제거

        if(VoiceMsg.indexOf("시작")>-1 || VoiceMsg.indexOf("선택")>-1){
            ui.use(VIDEO_START);
            Toast.makeText(context.getApplicationContext(), "재생",Toast.LENGTH_SHORT).show();
        }//동영상 선택

        else if(VoiceMsg.indexOf("멈춤")>-1 || VoiceMsg.indexOf("정지")>-1 || VoiceMsg.indexOf("재생")>-1){
            ui.use(VIDEO_PLAY_STOP);
            Toast.makeText(context.getApplicationContext(), "정지",Toast.LENGTH_SHORT).show();
        }//동영상 정지 및 재생

        else if(VoiceMsg.indexOf("종료")>-1){
            ui.use(UI.VIDEO_END);
            Toast.makeText(context.getApplicationContext(), "동영상 종료",Toast.LENGTH_SHORT).show();
        }//동영상 정지

        else if(VoiceMsg.indexOf("검색")>-1 || VoiceMsg.indexOf("메인")>-1 || VoiceMsg.indexOf("홈")>-1){
            ui.use(UI.SEARCH_OPEN);
            Toast.makeText(context.getApplicationContext(), "검색",Toast.LENGTH_SHORT).show();
        }//동영상 검색

        else if(VoiceMsg.indexOf("검색종료")>-1){
            ui.use(UI.SEARCH_CLOSE);
            Toast.makeText(context.getApplicationContext(), "검색 종료",Toast.LENGTH_SHORT).show();
        }//동영상 검색 종료

        else if(VoiceMsg.indexOf("왼쪽")>-1 || VoiceMsg.indexOf("이전")>-1){
            ui.use(UI.PREV_VIDEO);
            Toast.makeText(context.getApplicationContext(), "이전",Toast.LENGTH_SHORT).show();
        }//동영상 재생

        else if(VoiceMsg.indexOf("오른쪽")>-1 || VoiceMsg.indexOf("다음")>-1){
            ui.use(UI.NEXT_VIDEO);
            Toast.makeText(context.getApplicationContext(), "다음",Toast.LENGTH_SHORT).show();
        }//동영상 재생

        else{
            Toast.makeText(context.getApplicationContext(), VoiceMsg+" 다시 말씀해주세요",Toast.LENGTH_SHORT).show();
        }

        speechRecognizer.startListening(speechRecognizerIntent);

    }
}

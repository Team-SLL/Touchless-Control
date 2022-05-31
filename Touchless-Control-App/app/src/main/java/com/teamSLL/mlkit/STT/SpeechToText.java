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

import java.util.ArrayList;
import java.util.Locale;

public class SpeechToText {
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private SearchView searchView;
    int state = 0;

    public SpeechToText(Context context, SearchView searchView, ImageButton miconBtn, ImageButton micoffBtn){
        this.context = context;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);

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
                else if (miconBtn.getVisibility() == View.VISIBLE){
                    Toast.makeText(context.getApplicationContext(), "음성인식모드",Toast.LENGTH_SHORT).show();
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

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                if (state == 1){
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    searchView.setQuery(data.get(0), true);
                    state = 0;
                }
                else if (miconBtn.getVisibility() == View.VISIBLE){
                    String key= "";
                    key = SpeechRecognizer.RESULTS_RECOGNITION;
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String[] rs = new String[data.size()];
                    data.toArray(rs);;
                    FuncVoiceOrderCheck(rs[0]);
                    speechRecognizer.startListening(speechRecognizerIntent);
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
    private void FuncVoiceOrderCheck(String VoiceMsg){
        if(VoiceMsg.length()<1)return;

        VoiceMsg=VoiceMsg.replace(" ","");//공백제거

        if(VoiceMsg.indexOf("재생")>-1 || VoiceMsg.indexOf("선택")>-1){
            Toast.makeText(context.getApplicationContext(), "재생",Toast.LENGTH_SHORT).show();
        }//동영상 재생

        if(VoiceMsg.indexOf("검색")>-1 ){
            Toast.makeText(context.getApplicationContext(), "검색",Toast.LENGTH_SHORT).show();
        }//동영상 검색

        if(VoiceMsg.indexOf("멈춤")>-1 || VoiceMsg.indexOf("정지")>-1){
            Toast.makeText(context.getApplicationContext(), "정지",Toast.LENGTH_SHORT).show();
        }//동영상 재생

        if(VoiceMsg.indexOf("왼쪽")>-1 || VoiceMsg.indexOf("이전")>-1){
            Toast.makeText(context.getApplicationContext(), "이전",Toast.LENGTH_SHORT).show();
        }//동영상 재생

        if(VoiceMsg.indexOf("오른쪽")>-1 || VoiceMsg.indexOf("다음")>-1){
            Toast.makeText(context.getApplicationContext(), "다음",Toast.LENGTH_SHORT).show();
        }//동영상 재생

    }
}

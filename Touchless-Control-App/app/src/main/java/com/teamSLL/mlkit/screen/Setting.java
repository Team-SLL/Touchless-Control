package com.teamSLL.mlkit.screen;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.teamSLL.mlkit.R;
import com.teamSLL.mlkit.screen.UI;
import com.teamSLL.mlkit.facedetector.MotionRecongition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Setting {

    Activity activity;
    Context context;

    ArrayList<Spinner> main_spinners, video_spinners, search_spinners;
    EditText headms, mouthms, eyems;


    public Setting(Activity activity, Context context){
        this.activity = activity;
        this.context = context;

        main_spinners = new ArrayList<>();
        video_spinners = new ArrayList<>();
        search_spinners = new ArrayList<>();

        main_spinners.add(spinner(R.id.spinner_main_1));
        main_spinners.add(spinner(R.id.spinner_main_2));
        main_spinners.add(spinner(R.id.spinner_main_3));
        main_spinners.add(spinner(R.id.spinner_main_4));

        video_spinners.add(spinner(R.id.spinner_video_1));
        video_spinners.add(spinner(R.id.spinner_video_2));

        search_spinners.add(spinner(R.id.spinner_search_1));

        headms = (EditText) activity.findViewById(R.id.head_rec_ms);
        mouthms = (EditText) activity.findViewById(R.id.mouth_rec_ms);
        eyems = (EditText) activity.findViewById(R.id.eye_rec_ms);

        spinnerSetting();

        activity.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.onBackPressed();
                //뒤로가기, 프레그먼트 닫기
            }
        });

        activity.findViewById(R.id.reset_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = activity.getSharedPreferences("spinner", activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit(); //sharedPreferences를 제어할 editor를 선언

                editor.putInt(String.valueOf(UI.PREV_VIDEO), MotionRecongition.HEAD_LEFT);
                editor.putInt(String.valueOf(UI.NEXT_VIDEO), MotionRecongition.HEAD_RIGHT);
                editor.putInt(String.valueOf(UI.VIDEO_START), MotionRecongition.HEAD_UP);
                editor.putInt(String.valueOf(UI.SEARCH_OPEN), MotionRecongition.MOUTH_OPEN);

                editor.putInt(String.valueOf(UI.VIDEO_END), MotionRecongition.HEAD_DOWN);
                editor.putInt(String.valueOf(UI.VIDEO_PLAY_STOP), MotionRecongition.EYE_CLOSED_SHORT);

                editor.putInt(String.valueOf(UI.SEARCH_CLOSE), MotionRecongition.HEAD_DOWN);

                editor.putString("head_ms", "300");
                editor.putString("mouth_ms", "1000");
                editor.putString("eye_ms", "1000");

                editor.commit();

                spinnerSetting();
                Toast.makeText(context, "기본값으로 저장하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        activity.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkSpinnersHasSameValue(main_spinners)){
                    Toast.makeText(context,"메인화면에서 중복 동작이 있습니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!checkSpinnersHasSameValue(video_spinners)){
                    Toast.makeText(context,"비디오 화면에서 중복 동작이 있습니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!checkSpinnersHasSameValue(search_spinners)){
                    Toast.makeText(context,"검색 화면에서 중복 동작이 있습니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences sharedPreferences = activity.getSharedPreferences("spinner", activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit(); //sharedPreferences를 제어할 editor를 선언

                editor.putInt(String.valueOf(UI.PREV_VIDEO), main_spinners.get(0).getSelectedItemPosition());
                editor.putInt(String.valueOf(UI.NEXT_VIDEO), main_spinners.get(1).getSelectedItemPosition());
                editor.putInt(String.valueOf(UI.VIDEO_START), main_spinners.get(2).getSelectedItemPosition());
                editor.putInt(String.valueOf(UI.SEARCH_OPEN), main_spinners.get(3).getSelectedItemPosition());

                editor.putInt(String.valueOf(UI.VIDEO_END), video_spinners.get(0).getSelectedItemPosition());
                editor.putInt(String.valueOf(UI.VIDEO_PLAY_STOP), video_spinners.get(1).getSelectedItemPosition());

                editor.putInt(String.valueOf(UI.SEARCH_CLOSE), search_spinners.get(0).getSelectedItemPosition());

                editor.putString("head_ms", headms.getText().toString());
                editor.putString("mouth_ms", mouthms.getText().toString());
                editor.putString("eye_ms", eyems.getText().toString());

                editor.commit();

                Toast.makeText(context, "저장되었습니다", Toast.LENGTH_SHORT).show();
                activity.onBackPressed();
                // 저장, 변경사항 저장하기
            }
        });

    }
    private Spinner spinner(int id){
        Spinner _spinner = (Spinner) activity.findViewById(id);
        List<String> list = Arrays.asList(activity.getResources().getStringArray(R.array.motion_array));

        ArrayAdapter adapter = new ArrayAdapter(context, R.layout.spinner_item, list);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        _spinner.setAdapter(adapter);

        return _spinner;
    }

    private boolean checkSpinnersHasSameValue(ArrayList<Spinner> spinners){
        HashSet<String> temp = new HashSet<String>();
        for(int i=0;i<spinners.size();i++){
            String str = spinners.get(i).getSelectedItem().toString();
            if(str.equals("선택 안함")) continue;
            if(temp.add(str) == false){
                Log.i("RESULT_STR",str);
                return false;
            }
        }
        return true;
    }

    private void spinnerSetting(){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("spinner", activity.MODE_PRIVATE);    // test 이름의 기본모드 설정, 만약 test key값이 있다면 해당 값을 불러옴.

        main_spinners.get(0).setSelection(sharedPreferences.getInt(String.valueOf(UI.PREV_VIDEO), MotionRecongition.HEAD_LEFT));
        main_spinners.get(1).setSelection(sharedPreferences.getInt(String.valueOf(UI.NEXT_VIDEO), MotionRecongition.HEAD_RIGHT));
        main_spinners.get(2).setSelection(sharedPreferences.getInt(String.valueOf(UI.VIDEO_START), MotionRecongition.HEAD_UP));
        main_spinners.get(3).setSelection(sharedPreferences.getInt(String.valueOf(UI.SEARCH_OPEN), MotionRecongition.MOUTH_OPEN));

        video_spinners.get(0).setSelection(sharedPreferences.getInt(String.valueOf(UI.VIDEO_END), MotionRecongition.HEAD_DOWN));
        video_spinners.get(1).setSelection(sharedPreferences.getInt(String.valueOf(UI.VIDEO_PLAY_STOP), MotionRecongition.EYE_CLOSED_SHORT));

        search_spinners.get(0).setSelection(sharedPreferences.getInt(String.valueOf(UI.SEARCH_CLOSE), MotionRecongition.HEAD_DOWN));

        headms.setText(sharedPreferences.getString("head_ms", "300"));
        mouthms.setText(sharedPreferences.getString("mouth_ms","1000"));
        eyems.setText(sharedPreferences.getString("eye_ms", "1000"));
    }

    public ArrayList<ArrayList<Short>> getSetting(){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("spinner", activity.MODE_PRIVATE);    // test 이름의 기본모드 설정, 만약 test key값이 있다면 해당 값을 불러옴.

        ArrayList<ArrayList<Short>> motionToUI = new ArrayList<>(); // 얼굴 움직임 - UI 조작을 array list 에 넣어서 관리
        ArrayList<Short> mainScreen = new ArrayList<>();
        ArrayList<Short> videoScreen = new ArrayList<>();
        ArrayList<Short> searchScreen = new ArrayList<>();
        for(int i=0;i<8;i++){
            mainScreen.add(UI.NONE);
            videoScreen.add(UI.NONE);
            searchScreen.add(UI.NONE);
        }

        mainScreen.set(sharedPreferences.getInt(String.valueOf(UI.PREV_VIDEO),MotionRecongition.HEAD_LEFT), UI.PREV_VIDEO);
        mainScreen.set(sharedPreferences.getInt(String.valueOf(UI.NEXT_VIDEO),MotionRecongition.HEAD_RIGHT), UI.NEXT_VIDEO);
        mainScreen.set(sharedPreferences.getInt(String.valueOf(UI.VIDEO_START),MotionRecongition.HEAD_UP), UI.VIDEO_START);
        mainScreen.set(sharedPreferences.getInt(String.valueOf(UI.SEARCH_OPEN),MotionRecongition.MOUTH_OPEN), UI.SEARCH_OPEN);
        mainScreen.set(MotionRecongition.EYE_CLOSED_LONG, UI.CLOSE_APP);
        mainScreen.set(0, (short) 0);

        videoScreen.set(sharedPreferences.getInt(String.valueOf(UI.VIDEO_END),MotionRecongition.HEAD_DOWN), UI.VIDEO_END);
        videoScreen.set(sharedPreferences.getInt(String.valueOf(UI.VIDEO_PLAY_STOP),MotionRecongition.EYE_CLOSED_SHORT), UI.VIDEO_PLAY_STOP);
        videoScreen.set(MotionRecongition.EYE_CLOSED_LONG, UI.CLOSE_APP);
        videoScreen.set(0, (short) 0);

        searchScreen.set(sharedPreferences.getInt(String.valueOf(UI.SEARCH_CLOSE),MotionRecongition.HEAD_DOWN), UI.SEARCH_CLOSE);
        searchScreen.set(MotionRecongition.EYE_CLOSED_LONG, UI.CLOSE_APP);
        searchScreen.set(0, (short) 0);

        motionToUI.add(mainScreen);
        motionToUI.add(videoScreen);
        motionToUI.add(searchScreen);

        return motionToUI;
    }
    public int getHeadMs(){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("spinner", activity.MODE_PRIVATE);
        return Integer.parseInt(sharedPreferences.getString("head_ms", "300"));
    }
    public int getMouthMs(){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("spinner", activity.MODE_PRIVATE);
        return Integer.parseInt(sharedPreferences.getString("mouth_ms", "1000"));
    }
    public int getEyeMs(){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("spinner", activity.MODE_PRIVATE);
        return Integer.parseInt(sharedPreferences.getString("eye_ms", "1000"));
    }
}

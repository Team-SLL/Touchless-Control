package com.teamSLL.mlkit.screen;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.teamSLL.mlkit.R;
import com.teamSLL.mlkit.STT.SpeechToText;
import com.teamSLL.mlkit.youtube.PopularRunnable;
import com.teamSLL.mlkit.youtube.SearchRunnable;
import com.teamSLL.mlkit.youtube.YoutubeRunnable;

import java.util.ArrayList;

public class UI {
    public static final String KEY = "AIzaSyClXQPS7Ex7AGY7l3JCKVwe6er1lA5wj8E";

    public static final short NONE = 0;
    public static final short VIDEO_START = 1;
    public static final short VIDEO_END = 2;
    public static final short VIDEO_PLAY_STOP = 3;
    public static final short NEXT_VIDEO = 5;
    public static final short PREV_VIDEO = 6;
    public static final short SEARCH_OPEN = 7;
    public static final short SEARCH_CLOSE = 8;

    // youtube UI items
    private Activity activity;
    private Context context;
    private recyclerViewAdapter adapter;

    int MAX_LEN = 10;  // 1 ~ 50, 새로고침, 이후에 50개는 어떻게 불러올것인가
    public boolean isFullScreen = false;
    public YouTubePlayer youtubePlayer;

    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private SearchView searchView;
    private Button settingBtn;
    private SpeechToText stt;
    private SpeechRecognizer speechRecognizer;
    private DrawerLayout settingLayout;

    private ArrayList<VideoInfo> videoInfos = new ArrayList<>(); // 현재 영상 리스트

    public UI(Activity activity, Context context) {

        this.activity = activity;
        this.context = context;

        this.searchView = activity.findViewById(R.id.search_view);

        this.settingBtn = activity.findViewById(R.id.setting_button);
        this.settingLayout = (DrawerLayout) activity.findViewById(R.id.drawer);

        settingLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(settingLayout.isDrawerOpen(Gravity.RIGHT)){
                    settingLayout.closeDrawer(Gravity.RIGHT);
                }else{
                    settingLayout.openDrawer(Gravity.RIGHT);
                }

            }
        });

        if(this.searchView != null){
            this.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    updateUI(new SearchRunnable(UI.KEY, s, MAX_LEN));
                    closeSearchView();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return true;
                }
            });
            stt = new SpeechToText(context, searchView);
        }

        // 유튜브 영상을 재생할 프레그먼트
        YouTubePlayerFragment youtubePlayerFragment = (YouTubePlayerFragment) activity.getFragmentManager().findFragmentById(R.id.youtube_player_fragment); //유튜브 재생 프래그먼트, 유튜브 영상 재생용
        youtubePlayerFragment.initialize(KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean b) {
                youtubePlayer = player;
                youtubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                    @Override
                    public void onFullscreen(boolean b) {
                        isFullScreen = b;
                        use(VIDEO_PLAY_STOP);
                    }
                });
            }
            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.e("Failure", "youTubePlayerFragment onInitializationFailure");
                Log.e("youTubePlayerFragment", String.valueOf(youTubeInitializationResult));
            }
        });

        // 영상 리스트뷰 셋팅
        this.recyclerView = activity.findViewById(R.id.rvVideos); // 영상 목록을 담을 recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        this.manager = (LinearLayoutManager) recyclerView.getLayoutManager();

        updateUI(new PopularRunnable(KEY, MAX_LEN));
    }

    public void updateUI(YoutubeRunnable requestedRunnable){
        Handler handler = new Handler(Looper.getMainLooper()) { // 쓰레드에서 값이 생성되어 메세지를 보내면 실행
            @Override
            public void handleMessage(@NonNull Message msg) {
                videoInfos = requestedRunnable.getVideoInfos();
                adapter = new recyclerViewAdapter(context, videoInfos); // 리사이클러 뷰 생성
                adapter.setClickListener(new recyclerViewAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if(position == 0 || position == MAX_LEN+1) return;
                        String id = adapter.getItem(position);
                        youtubePlayer.cueVideo(id);
                        youtubePlayer.setFullscreen(true);
                    }
                });
                recyclerView.setAdapter(adapter);
            }
        };

        new Thread() {
            @Override
            public void run(){
                try {
                    Thread thread = new Thread(requestedRunnable);
                    thread.start(); // Popular/Search 쓰레드 실행해서 ArrayList<VideoInfo> 받아옴
                    thread.join();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message msg = handler.obtainMessage();
                handler.sendMessage(msg);
            }
        }.start();
    }

    // 얼굴인식 결과에 대해 이벤트 실행
    public void use(Short command){
        if(command == null) return;
        int position = 0;
        switch(command){
            case NONE:
                break;
            case VIDEO_START:
                if(isFullScreen == false || !youtubePlayer.isPlaying() || !searchView.isFocused()){
                    isFullScreen = true;
                    position = manager.findFirstCompletelyVisibleItemPosition();
                    recyclerView.findViewHolderForLayoutPosition(position+1).itemView.performClick();
                }
                break;
            case VIDEO_END:
                if(isFullScreen == true || youtubePlayer.isPlaying()){
                    isFullScreen = false;
                    youtubePlayer.setFullscreen(false);
                }
                break;
            case VIDEO_PLAY_STOP:
                if(youtubePlayer.isPlaying()){
                    youtubePlayer.pause();
                }else{
                    youtubePlayer.play();
                }
                break;
            case NEXT_VIDEO:
                if(isFullScreen || youtubePlayer.isPlaying()) break; // 동영상이 재생중이 아닌 경우에만 실행
                position = manager.findLastCompletelyVisibleItemPosition();
                recyclerView.smoothScrollToPosition(position == MAX_LEN+1 ? MAX_LEN+1: position+1);
                break;
            case PREV_VIDEO:
                if(isFullScreen || youtubePlayer.isPlaying()) break;
                position = manager.findFirstCompletelyVisibleItemPosition();
                recyclerView.smoothScrollToPosition(position == 0 ? 0: position-1);
                break;
            case SEARCH_OPEN:
                if(isFullScreen || youtubePlayer.isPlaying()) break;
                searchView.setIconified(false);
                searchView.setQuery("",false);
                // updateUI(new PopularRunnable(KEY, MAX_LEN)); // 임시로 SEARCH 명령어가 들어오면 인기영상이 출력되게 함
               // updateUI(new SearchRunnable(KEY, "고양이", MAX_LEN));
                break;
            case SEARCH_CLOSE:
                closeSearchView();
                break;
            default:
                Log.e("Use Ui", "wrong event command");
                break;
        }
    }

    // 검색창 활성화 되어있으면 False
    public boolean isSearchViewOpened(){
        return searchView.isHovered();
    }
    // 검색창 종료 함수
    public void closeSearchView(){
        searchView.setQueryHint("");
        searchView.clearFocus();
    }
    public boolean isSettingOpen(){
        return settingLayout.isDrawerOpen(Gravity.RIGHT);
    }
    public void closeSetting(){ settingLayout.closeDrawer(Gravity.RIGHT); }
}

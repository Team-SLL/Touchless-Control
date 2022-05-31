package com.teamSLL.mlkit.screen;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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

    public boolean isFullScreen = false;
    public YouTubePlayer youtubePlayer;

    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private SearchView searchView;

    private ImageButton settingBtn;  // 이미지 버튼 추가
    private ImageButton miconBtn;
    private ImageButton micoffBtn;
    private ImageButton homeBtn;
    private ImageButton searchBtn;

    private SpeechToText stt;
    private SpeechRecognizer speechRecognizer;
    private DrawerLayout settingLayout;

    private boolean refresh = false;
    private String nextVideoToken = "";
    private String searchText = "";

    private ArrayList<VideoInfo> videoInfos = new ArrayList<>(); // 현재 영상 리스트

    public UI(Activity activity, Context context) {

        this.activity = activity;
        this.context = context;

        this.settingBtn = activity.findViewById(R.id.setting_button);   //이미지 버튼 연결
        this.settingLayout = (DrawerLayout) activity.findViewById(R.id.drawer);
        this.micoffBtn = activity.findViewById(R.id.micoff_button);
        this.miconBtn = activity.findViewById(R.id.micon_button);
        this.homeBtn = activity.findViewById(R.id.home_button);
        this.searchBtn = activity.findViewById(R.id.search_button);

        this.searchView = activity.findViewById(R.id.search_view);
        searchView.setVisibility(View.INVISIBLE);
        miconBtn.setVisibility(View.GONE);  //음섬 인식 비활성화

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

        this.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchText = s;
                nextVideoToken = "";
                updateUI(new SearchRunnable(s));
                closeSearchView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });
        stt = new SpeechToText(context, searchView, miconBtn, micoffBtn); //음성인식 활성화

        homeBtn.setOnClickListener(new View.OnClickListener(){  // 인기리스트로 돌아가기
            @Override
            public void onClick(View v){
                searchText = "";
                nextVideoToken = "";
                homeBtn.setBackgroundResource(R.drawable.round_button_click);
                searchView.setVisibility(View.INVISIBLE);
                searchBtn.setBackgroundResource(R.drawable.round_button);
                updateUI(new PopularRunnable()); // 인기리스트 불러오기
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeBtn.setBackgroundResource(R.drawable.round_button);
                searchView.setVisibility(View.VISIBLE);
                searchBtn.setBackgroundResource(R.drawable.round_button_click);
            }
        });

        if(micoffBtn.getVisibility() == View.GONE){
            stt = new SpeechToText(context, searchView, miconBtn, micoffBtn); //음성인식 활성화
        }

        // 유튜브 영상을 재생할 프레그먼트
        YouTubePlayerFragment youtubePlayerFragment = (YouTubePlayerFragment) activity.getFragmentManager().findFragmentById(R.id.youtube_player_fragment); //유튜브 재생 프래그먼트, 유튜브 영상 재생용
        youtubePlayerFragment.initialize(YoutubeRunnable.KEY, new YouTubePlayer.OnInitializedListener() {
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

        updateUI(new PopularRunnable());
    }

    public void updateUI(YoutubeRunnable requestedRunnable){
        Handler handler = new Handler(Looper.getMainLooper()) { // 쓰레드에서 값이 생성되어 메세지를 보내면 실행
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(nextVideoToken == ""){
                    videoInfos = requestedRunnable.getVideoInfos();
                    videoInfos.add(0,new VideoInfo("", "","", "", "", null, null));
                    videoInfos.add(new VideoInfo("", "","", "", "", null, null));
                    adapter = new recyclerViewAdapter(context, videoInfos); // 리사이클러 뷰 생성
                    adapter.setClickListener(new recyclerViewAdapter.ItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            if(position == 0 || position == videoInfos.size()) return;
                            String id = adapter.getItem(position);
                            youtubePlayer.cueVideo(id);
                            youtubePlayer.setFullscreen(true);
                        }
                    });

                    recyclerView.setAdapter(adapter);
                    recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                        @Override
                        public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                            int position = manager.findLastCompletelyVisibleItemPosition();
                            if(position == videoInfos.size()-1)
                                use(NEXT_VIDEO);
                        }
                    });
                }else{
                    refresh = false;
                    adapter.addVideos(requestedRunnable.getVideoInfos());
                }
                nextVideoToken = requestedRunnable.getNextVideoToken();
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

                if(position != videoInfos.size()-1){
                    recyclerView.smoothScrollToPosition(position + 1);

                }else if(!refresh){
                    refresh = true;
                    if(searchView.getVisibility() == View.INVISIBLE)
                        updateUI(new PopularRunnable(nextVideoToken));
                    else updateUI(new SearchRunnable(searchText, nextVideoToken));
                }
                break;
            case PREV_VIDEO:
                if(isFullScreen || youtubePlayer.isPlaying()) break;
                position = manager.findFirstCompletelyVisibleItemPosition();
                position = position == 0 ? 1 : position;
                recyclerView.smoothScrollToPosition(position - 1);
            //    highlightItem(position, true);
                break;
            case SEARCH_OPEN:
                if(isFullScreen || youtubePlayer.isPlaying()) break;
                searchBtn.performClick();
                searchView.setIconified(false);
                searchView.setQuery("",false);
                break;
            case SEARCH_CLOSE:
                closeSearchView();
                break;
            default:
                Log.e("Use Ui", "wrong event command");
                break;
        }
    }

    private void highlightItem(int position, boolean left){
        if(left){
            recyclerView.findViewHolderForLayoutPosition(position+1).itemView.setBackgroundResource(R.drawable.round_recycler);  // 복구
            recyclerView.findViewHolderForLayoutPosition(position).itemView.setBackgroundResource(R.drawable.zoom_recyclerview); // 강조
        }
        else{
            recyclerView.findViewHolderForLayoutPosition(position-1).itemView.setBackgroundResource(R.drawable.round_recycler);  // 복구
            recyclerView.findViewHolderForLayoutPosition(position).itemView.setBackgroundResource(R.drawable.zoom_recyclerview); // 강조
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

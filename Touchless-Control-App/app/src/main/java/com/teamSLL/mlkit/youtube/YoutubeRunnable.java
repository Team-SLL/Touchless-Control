package com.teamSLL.mlkit.youtube;

import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.teamSLL.mlkit.adapter.VideoInfo;

import java.io.IOException;
import java.util.ArrayList;

public class YoutubeRunnable implements Runnable{
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new GsonFactory();
    protected YouTube youtube;
    protected String KEY;
    protected ArrayList<VideoInfo> videoInfos = new ArrayList<>();


    public YoutubeRunnable(String KEY){
        this.KEY = KEY;
        this.youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {}
        }).setApplicationName("youtube-touchless").build();
    }

    @Override
    public void run() {
        Log.e("YoutubeThread", "This is abstract class");
    }

    public ArrayList<VideoInfo> getVideoInfos(){
        return this.videoInfos;
    }
}

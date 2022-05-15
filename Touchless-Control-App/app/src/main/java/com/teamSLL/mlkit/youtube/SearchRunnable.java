package com.teamSLL.mlkit.youtube;

import android.util.Log;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.teamSLL.mlkit.adapter.VideoInfo;

import java.io.IOException;
import java.util.List;

public class SearchRunnable extends YoutubeRunnable {
    private int MAX_LEN; // Quota = 100 + MAX_LEN*2
    private String keyword;

    public SearchRunnable(String KEY, String keyword){
        super(KEY);
        this.MAX_LEN = 5;
        this.keyword = keyword;
    }
    public SearchRunnable(String KEY, String keyword, int MAX_LEN){
        super(KEY);
        this.MAX_LEN = MAX_LEN;
        this.keyword = keyword;
    }

    public void run(){
        YouTube.Search.List search;
        try {
            search = youtube.search().list("id");

            search.setQ(keyword);
            search.setType("video");
            search.setFields("items(id/videoId)");
            search.setMaxResults((long) MAX_LEN);
            search.setKey(KEY);

            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();

            videoInfos.clear();
            videoInfos.add(new VideoInfo("", "","", "", "", null, null));
            for (int i = 0; i < searchResultList.size(); i++) {
                String videoID = searchResultList.get(i).getId().getVideoId();

                VideoRunnable getVideoRunnable = new VideoRunnable(KEY, videoID);
                Thread thread = new Thread(getVideoRunnable);
                thread.start();
                thread.join();
                videoInfos.add(getVideoRunnable.getVideoInfo());
            }
            videoInfos.add(new VideoInfo("", "","", "", "", null, null));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}

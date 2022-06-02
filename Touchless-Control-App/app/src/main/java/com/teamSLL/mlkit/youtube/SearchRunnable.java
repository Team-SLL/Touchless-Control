package com.teamSLL.mlkit.youtube;

import android.util.Log;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.teamSLL.mlkit.screen.VideoInfo;

import java.io.IOException;
import java.util.List;

public class SearchRunnable extends YoutubeRunnable {
    private int MAX_LEN = 10; // Quota = 100 + MAX_LEN*2
    private String keyword;
    private String nextToken = "";

    public SearchRunnable(String keyword){
        this.keyword = keyword;
    }
    public SearchRunnable(String keyword, String nextToken){
        this.keyword = keyword;
        this.nextToken = nextToken;
    }

    public void run(){
        YouTube.Search.List search;
        try {
            search = youtube.search().list("id");

            search.setQ(keyword);
            search.setType("video");
            search.setMaxResults((long) MAX_LEN);
            search.setKey(KEY);
            if(nextToken != "")
                search.setPageToken(nextToken);

            SearchListResponse response = search.execute();
            List<SearchResult> searchResultList = response.getItems();
            this.nextVideoToken = response.getNextPageToken();

            videoInfos.clear();
            for (int i = 0; i < searchResultList.size(); i++) {
                String videoID = searchResultList.get(i).getId().getVideoId();

                VideoRunnable getVideoRunnable = new VideoRunnable(videoID);
                Thread thread = new Thread(getVideoRunnable);
                thread.start();
                thread.join();
                videoInfos.add(getVideoRunnable.getVideoInfo());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Log.e("Runnable Error",e.toString());
        }
    }

}

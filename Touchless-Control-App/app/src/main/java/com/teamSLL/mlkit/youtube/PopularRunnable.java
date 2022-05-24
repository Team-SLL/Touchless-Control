package com.teamSLL.mlkit.youtube;

import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
import com.teamSLL.mlkit.screen.VideoInfo;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class PopularRunnable extends YoutubeRunnable {
    private int MAX_LEN; // Quota = 1 + MAX_LEN

    public PopularRunnable(String KEY){
        super(KEY);
        this.MAX_LEN = 5;
    }
    public PopularRunnable(String KEY, int MAX_LEN){
        super(KEY);
        this.MAX_LEN = MAX_LEN;
    }

    public void run(){
        YouTube.Videos.List popular;
        try {
            popular = youtube.videos().list("id,snippet,statistics");

            popular.setFields("items");
            popular.setChart("mostPopular");
            popular.setMaxResults((long) MAX_LEN);
            popular.setRegionCode("kr");
            popular.setKey(KEY);
            VideoListResponse response = popular.execute();
            List<Video> resultList = response.getItems();
            videoInfos.clear();
            videoInfos.add(new VideoInfo("", "","", "", "", null, null));

            for (int i = 0; i < resultList.size(); i++) {
                Video result = resultList.get(i);

                VideoSnippet snippet = result.getSnippet();
                VideoStatistics statistics = result.getStatistics();

                String videoID = result.getId();

                String channelID = snippet.getChannelId();
                String channelTitle = snippet.getChannelTitle();

                DateTime uploadedTime = snippet.getPublishedAt();
                String videoTitle = snippet.getTitle();

                BigInteger views = statistics.getViewCount();
                Log.i("UPDATE_UI",videoTitle);
                ThumbnailRunnable getThumbnail = new ThumbnailRunnable(KEY, channelID);
                Thread thread = new Thread(getThumbnail);
                thread.start();
                thread.join();
                String channelThumbnail = getThumbnail.getChannelThumbnail();

                videoInfos.add(new VideoInfo(videoID, videoTitle, channelID, channelTitle, channelThumbnail, uploadedTime, views));
            }
            videoInfos.add(new VideoInfo("", "","", "", "", null, null));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Log.e("Runnable Error",e.toString());
        }

    }

}

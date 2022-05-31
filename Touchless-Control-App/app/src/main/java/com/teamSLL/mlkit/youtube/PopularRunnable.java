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
    private int MAX_LEN = 3; // Quota = 1 + MAX_LEN
    private String nextToken = "";

    public PopularRunnable(){
    }
    public PopularRunnable(String nextToken){
        this.nextToken = nextToken;
    }

    public void run(){
        YouTube.Videos.List popular;
        try {
            popular = youtube.videos().list("id,snippet,statistics");

            popular.setChart("mostPopular");
            popular.setMaxResults((long) MAX_LEN);
            popular.setRegionCode("kr");
            popular.setKey(KEY);
            if(nextToken != "")
                popular.setPageToken(nextToken);

            VideoListResponse response = popular.execute();
            List<Video> resultList = response.getItems();


            this.nextVideoToken = response.getNextPageToken();
            videoInfos.clear();
            for (int i = 0; i < resultList.size(); i++) {
                Video result = resultList.get(i);

                VideoSnippet snippet = result.getSnippet();
                VideoStatistics statistics = result.getStatistics();

                String videoID = result.getId();
                String videoTitle = snippet.getTitle();

                String channelID = snippet.getChannelId();
                String channelTitle = snippet.getChannelTitle();

                DateTime uploadedTime = snippet.getPublishedAt();
                BigInteger views = statistics.getViewCount();

                ThumbnailRunnable getThumbnail = new ThumbnailRunnable(channelID);
                Thread thread = new Thread(getThumbnail);
                thread.start();
                thread.join();
                String channelThumbnail = getThumbnail.getChannelThumbnail();

                videoInfos.add(new VideoInfo(videoID, videoTitle, channelID, channelTitle, channelThumbnail, uploadedTime, views));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Log.e("Runnable Error",e.toString());
        }

    }

}

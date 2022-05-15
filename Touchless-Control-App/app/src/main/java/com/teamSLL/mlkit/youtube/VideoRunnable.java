package com.teamSLL.mlkit.youtube;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
import com.teamSLL.mlkit.adapter.VideoInfo;

import java.io.IOException;
import java.math.BigInteger;

public class VideoRunnable extends YoutubeRunnable {

    private String videoID;
    private VideoInfo videoInfo;

    public VideoRunnable(String KEY, String videoID){
        super(KEY);
        this.videoID = videoID;
    }

    public void run(){

        YouTube.Videos.List popular;

        try {
            popular = youtube.videos().list("snippet,statistics");

            popular.setFields("items");
            popular.setId(videoID);
            popular.setKey(KEY);

            VideoListResponse response = popular.execute();
            Video result = response.getItems().get(0);

            VideoSnippet snippet = result.getSnippet();
            VideoStatistics statistics = result.getStatistics();

            String channelID = snippet.getChannelId();
            String channelTitle = snippet.getChannelTitle();
            DateTime uploadedTime = snippet.getPublishedAt();
            String videoTitle = snippet.getTitle();
            BigInteger views = statistics.getViewCount();

            ThumbnailRunnable getThumbnail = new ThumbnailRunnable(KEY, channelID);
            Thread thread = new Thread(getThumbnail);
            thread.start();
            thread.join();
            String channelThumbnail = getThumbnail.getChannelThumbnail();
            videoInfo = new VideoInfo(videoID, videoTitle, channelID, channelTitle, channelThumbnail, uploadedTime, views);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public VideoInfo getVideoInfo(){
        return this.videoInfo;
    }
}

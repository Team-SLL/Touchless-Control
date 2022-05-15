package com.teamSLL.mlkit.adapter;

import com.google.api.client.util.DateTime;

import java.math.BigInteger;

public class VideoInfo {
    public String videoID;
    public String videoTitle;
    public String channelThumbnail;
    public String channelTitle;
    public String channelID;
    public DateTime uploadedTime;
    public BigInteger views;

    public VideoInfo(String videoID, String videoTitle, String channelID, String channelTitle, String channelThumbnail, DateTime uploadedTime, BigInteger views){
        this.videoID = videoID;
        this.videoTitle = videoTitle;
        this.channelID = channelID;
        this.channelTitle = channelTitle;
        this.channelThumbnail = channelThumbnail;
        this.uploadedTime = uploadedTime;
        this.views = views;
    }
}

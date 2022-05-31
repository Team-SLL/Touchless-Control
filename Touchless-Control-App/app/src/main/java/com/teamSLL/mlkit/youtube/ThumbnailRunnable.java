package com.teamSLL.mlkit.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;

import java.io.IOException;

public class ThumbnailRunnable extends YoutubeRunnable {
    private String channelID;
    private String channelThumbnail;

    public ThumbnailRunnable(String channelID){
        this.channelID = channelID;
    }

    public void run() {
        // 쓰레드 실행
        // 유튜브에 쿼리를 보내 영상 Json 리스트를 가져옴

//        String channelQuery = "https://www.googleapis.com/youtube/v3/channels?" +
//                "part=snippet&" +
//                "id=" + channelID + "&" +
//                "fields=items%2Fsnippet%2Fthumbnails%2Fdefault&" +
//                "key=" + KEY;
        YouTube.Channels.List channel;

        try {
            channel = youtube.channels().list("snippet");
            channel.setId(channelID);
            channel.setFields("items/snippet/thumbnails/default");
            channel.setKey(KEY);

            ChannelListResponse response = channel.execute();
            Channel result = response.getItems().get(0);

            this.channelThumbnail = result.getSnippet().getThumbnails().getDefault().getUrl();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getChannelThumbnail(){
        return this.channelThumbnail;
    }
}

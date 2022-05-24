package com.teamSLL.mlkit.screen;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.client.util.DateTime;
import com.squareup.picasso.Picasso;
import com.teamSLL.mlkit.R;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class recyclerViewAdapter extends RecyclerView.Adapter<recyclerViewAdapter.ViewHolder> {
    private List<VideoInfo> mVideoInfos;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    // data is passed into the
    public recyclerViewAdapter(Context context, List<VideoInfo> mVideoInfos) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mVideoInfos = mVideoInfos;
    }

    // inflates the row layout from xml when needed
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = (int) (parent.getWidth() / 3);
        view.setLayoutParams(layoutParams);

        return new ViewHolder(view);
    }

    void setImage(ImageView view, String url){
        if(url == "") {
            view.setImageResource(R.drawable.transparent);
            return;
        }
        Picasso.with(this.context).load(url).placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(view, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                    }
                    @Override
                    public void onError() {
                    }
                });
    }

    public String makeViews(BigInteger Views){
        if(Views == null) return "";
        float views = Views.floatValue();
        if(views < 1000){
            return "조회수 " + views + "회";
        }
        views = views/1000;
        if(views < 10){
            return "조회수 " + String.format("%.1f",views) + "천회";
        }
        views = views/10;
        if(views < 10000){
            if(views < 10)
                return "조회수 " + String.format("%.1f",views) + "만회";
            return "조회수 " + String.format("%.0f",views) + "만회";
        }
        views = views/10000;
        return "조회수 " + String.format("%.1f",views) + "억회";
    }

    public String makeUploadedTime(DateTime uploadedTime){
        if(uploadedTime == null) return "";

        String t = "초 전";
        Date currentTime = Calendar.getInstance().getTime();
        long result = (currentTime.getTime() - uploadedTime.getValue())/1000;
        if(result > 60){
            result = result/60;
            t = "분 전";
        }
        if(result > 60){
            result = result/60;
            t = "시간 전";
        }
        if(result > 24){
            result = result/24;
            t = "일 전";
        }

        return result + t;
    }
    // binds the data to the view and textview in each row
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoInfo videoinfo = mVideoInfos.get(position);

        holder.videoTitle.setText(videoinfo.videoTitle);
        holder.channelTitle.setText(videoinfo.channelTitle);
        holder.views.setText(makeViews(videoinfo.views));
        holder.uploadedTime.setText(makeUploadedTime(videoinfo.uploadedTime));

        ShapeDrawable mDrawable = new ShapeDrawable(new OvalShape());
        mDrawable.getPaint().setColor(Color.TRANSPARENT);
        holder.channelThumbnail.setBackground(mDrawable);
        holder.channelThumbnail.setClipToOutline(true);

        if(videoinfo.videoID == "")
            setImage(holder.videoThumbnail, "");
        else
            setImage(holder.videoThumbnail, "https://i.ytimg.com/vi/" + videoinfo.videoID + "/hqdefault.jpg");
        setImage(holder.channelThumbnail, videoinfo.channelThumbnail);


    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView videoThumbnail;
        ImageView channelThumbnail;
        TextView videoTitle;
        TextView channelTitle;
        TextView views;
        TextView uploadedTime;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        ViewHolder(View itemView) {
            super(itemView);
            videoThumbnail = itemView.findViewById(R.id.videoThumbnail);
            channelThumbnail = itemView.findViewById(R.id.channelThumbnail);
            videoTitle = itemView.findViewById(R.id.videoTitle);
            channelTitle = itemView.findViewById(R.id.channelTitle);
            views = itemView.findViewById(R.id.views);
            uploadedTime = itemView.findViewById(R.id.uploadedTime);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mVideoInfos.size();
    }

    // convenience method for getting data at click position
    public String getItem(int id) {
        return mVideoInfos.get(id).videoID;
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
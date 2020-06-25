package com.albertkhang.tunedaily.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.events.ShowMiniplayerEvent;
import com.albertkhang.tunedaily.events.UpdateCurrentTrackEvent;
import com.albertkhang.tunedaily.utils.DownloadTrackManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.models.Track;
import com.albertkhang.tunedaily.views.RoundImageView;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class TopChartAdapter extends RecyclerView.Adapter<TopChartAdapter.ViewHolder> {
    private ArrayList<Track> tracks = new ArrayList<>();
    private Context context;
    private SettingManager settingManager;

    public TopChartAdapter(Context context) {
        this.context = context;
        this.settingManager = SettingManager.getInstance(context);
    }

    public void update(ArrayList<Track> tracks) {
        this.tracks.clear();
        this.tracks.addAll(tracks);
        notifyDataSetChanged();
    }

    public void update() {
        notifyDataSetChanged();
    }

    public interface OnMoreListener {
        void onMoreListener(View view, int position);
    }

    private OnMoreListener onMoreListener;

    public void setOnMoreListener(OnMoreListener onMoreListener) {
        this.onMoreListener = onMoreListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_top_chart, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        handlePositionTextColor(holder.txtPosition, position + 1);

        holder.txtTitle.setText(tracks.get(position).getTitle());
        handleTitleColor(holder.txtTitle);

        holder.txtArtist.setText(tracks.get(position).getArtist());
        handleArtistColor(holder.txtArtist);

        handleCoverPlaceholderColor(holder.imgCover, position);

        holder.imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMoreListener.onMoreListener(holder.itemView, position);
            }
        });
        handleMoreIconColor(holder.imgMore);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new UpdateCurrentTrackEvent(tracks.get(position)));
                EventBus.getDefault().post(new ShowMiniplayerEvent());
            }
        });

        handleDownloadColor(holder.imgDownload);
        holder.imgDownload.setVisibility(View.GONE);
        DownloadTrackManager downloadTrackManager = new DownloadTrackManager(context);
        if (downloadTrackManager.isFileExists(tracks.get(position))) {
            holder.imgDownload.setVisibility(View.VISIBLE);
        }
    }

    private void handleMoreIconColor(ImageView imageView) {
        if (settingManager.isDarkTheme()) {
            imageView.setColorFilter(context.getResources().getColor(R.color.colorLight5));
        } else {
            imageView.setColorFilter(context.getResources().getColor(R.color.colorDark5));
        }
    }

    private void handleArtistColor(TextView textView) {
        if (settingManager.isDarkTheme()) {
            textView.setTextColor(context.getResources().getColor(R.color.colorLight4));
        } else {
            textView.setTextColor(context.getResources().getColor(R.color.colorDark4));
        }
    }

    private void handleDownloadColor(ImageView imageView) {
        if (settingManager.isDarkTheme()) {
            imageView.setColorFilter(context.getResources().getColor(R.color.colorLight4));
        } else {
            imageView.setColorFilter(context.getResources().getColor(R.color.colorDark4));
        }
    }

    private void handleTitleColor(TextView textView) {
        if (settingManager.isDarkTheme()) {
            textView.setTextColor(context.getResources().getColor(R.color.colorLight1));
        } else {
            textView.setTextColor(context.getResources().getColor(R.color.colorDark1));
        }
    }

    private void handleCoverPlaceholderColor(RoundImageView imgCover, int position) {
        if (settingManager.isDarkTheme()) {
            Glide.with(context).load(tracks.get(position).getCover()).placeholder(R.color.colorLight5).into(imgCover);
        } else {
            Glide.with(context).load(tracks.get(position).getCover()).placeholder(R.color.colorDark5).into(imgCover);
        }
    }

    private void handlePositionTextColor(TextView txtPosition, int position) {
        txtPosition.setText("0" + position);

        if (settingManager.isDarkTheme()) {
            txtPosition.setTextColor(context.getResources().getColor(R.color.colorLight5));
        } else {
            txtPosition.setTextColor(context.getResources().getColor(R.color.colorDark5));
        }

        switch (position) {
            case 1:
                txtPosition.setTextColor(context.getResources().getColor(R.color.colorSt1));
                txtPosition.setTypeface(Typeface.DEFAULT_BOLD);
                break;

            case 2:
                txtPosition.setTextColor(context.getResources().getColor(R.color.colorSt2));
                txtPosition.setTypeface(Typeface.DEFAULT_BOLD);
                break;

            case 3:
                txtPosition.setTextColor(context.getResources().getColor(R.color.colorSt3));
                txtPosition.setTypeface(Typeface.DEFAULT_BOLD);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtPosition;
        private RoundImageView imgCover;
        private TextView txtTitle;
        private TextView txtArtist;
        private ImageView imgMore;
        private ImageView imgDownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtPosition = itemView.findViewById(R.id.txtPosition);
            imgCover = itemView.findViewById(R.id.imgCover);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtArtist = itemView.findViewById(R.id.txtArtist);
            imgMore = itemView.findViewById(R.id.imgMore);
            imgDownload = itemView.findViewById(R.id.imgDownload);
        }
    }
}

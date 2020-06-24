package com.albertkhang.tunedaily.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.events.ShowMiniplayerEvent;
import com.albertkhang.tunedaily.events.UpdateCurrentTrackEvent;
import com.albertkhang.tunedaily.events.UpdateFavouriteTrack;
import com.albertkhang.tunedaily.services.MediaPlaybackService;
import com.albertkhang.tunedaily.utils.DownloadTrackManager;
import com.albertkhang.tunedaily.utils.PlaylistManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.models.Track;
import com.albertkhang.tunedaily.views.RoundImageView;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.ViewHolder> {
    private ArrayList<Track> tracks = new ArrayList<>();
    private Context context;
    private SettingManager settingManager;

    public TrackAdapter(Context context) {
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

    public Context getContext() {
        return context;
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
        View view = inflater.inflate(R.layout.item_track, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
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
        handleFavouriteIconColor(holder.imgFavourite, position);
        handleDownloadColor(holder.imgDownload);
        holder.imgDownload.setVisibility(View.GONE);
        DownloadTrackManager downloadTrackManager = new DownloadTrackManager(context);
        if (downloadTrackManager.isFileExists(tracks.get(position))) {
            holder.imgDownload.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new UpdateCurrentTrackEvent(tracks.get(position)));
                EventBus.getDefault().post(new ShowMiniplayerEvent());
            }
        });

        holder.imgFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PlaylistManager.getInstance().isContainInLikedSongs(tracks.get(position).getId())) {
                    PlaylistManager.getInstance().removeFromLikedSongs(tracks.get(position).getId());
                    holder.imgFavourite.setImageResource(R.drawable.ic_not_favourite);
                    if (settingManager.isDarkTheme()) {
                        holder.imgFavourite.setColorFilter(context.getResources().getColor(R.color.colorLight5));
                    } else {
                        holder.imgFavourite.setColorFilter(context.getResources().getColor(R.color.colorDark5));
                    }
                    Toast.makeText(context, "Removed \"" + tracks.get(position).getTitle() + "\" from liked songs", Toast.LENGTH_LONG).show();
                } else {
                    PlaylistManager.getInstance().addToLikedSongs(tracks.get(position).getId());
                    holder.imgFavourite.setColorFilter(context.getResources().getColor(R.color.colorMain3));
                    holder.imgFavourite.setImageResource(R.drawable.ic_favourite_blue);
                    Toast.makeText(context, "Added \"" + tracks.get(position).getTitle() + "\" in liked songs", Toast.LENGTH_LONG).show();
                }

                EventBus.getDefault().post(new UpdateFavouriteTrack(tracks.get(position).getId()));
            }
        });
    }

    private void handleDownloadColor(ImageView imageView) {
        if (settingManager.isDarkTheme()) {
            imageView.setColorFilter(context.getResources().getColor(R.color.colorLight4));
        } else {
            imageView.setColorFilter(context.getResources().getColor(R.color.colorDark4));
        }
    }

    private void handleMoreIconColor(ImageView imageView) {
        if (settingManager.isDarkTheme()) {
            imageView.setColorFilter(context.getResources().getColor(R.color.colorLight5));
        } else {
            imageView.setColorFilter(context.getResources().getColor(R.color.colorDark5));
        }
    }

    private void handleFavouriteIconColor(ImageView imageView, int position) {
        if (PlaylistManager.getInstance().isContainInLikedSongs(tracks.get(position).getId())) {
            imageView.setColorFilter(context.getResources().getColor(R.color.colorMain3));
            imageView.setImageResource(R.drawable.ic_favourite_blue);
        } else {
            imageView.setImageResource(R.drawable.ic_not_favourite);

            if (settingManager.isDarkTheme()) {
                imageView.setColorFilter(context.getResources().getColor(R.color.colorLight5));
            } else {
                imageView.setColorFilter(context.getResources().getColor(R.color.colorDark5));
            }
        }
    }

    private void handleArtistColor(TextView textView) {
        if (settingManager.isDarkTheme()) {
            textView.setTextColor(context.getResources().getColor(R.color.colorLight4));
        } else {
            textView.setTextColor(context.getResources().getColor(R.color.colorDark4));
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

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private RoundImageView imgCover;
        private TextView txtTitle;
        private TextView txtArtist;
        private ImageView imgMore;
        private ImageView imgFavourite;
        private ImageView imgDownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgCover = itemView.findViewById(R.id.imgCover);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtArtist = itemView.findViewById(R.id.txtArtist);
            imgFavourite = itemView.findViewById(R.id.imgFavourite);
            imgMore = itemView.findViewById(R.id.imgMore);
            imgDownload = itemView.findViewById(R.id.imgDownload);
        }
    }
}

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
import com.albertkhang.tunedaily.utils.Album;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.Track;
import com.albertkhang.tunedaily.views.RoundImageView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    private ArrayList<Album> albums = new ArrayList<>();
    private Context context;
    private SettingManager settingManager;

    public AlbumAdapter(Context context) {
        this.context = context;
        this.settingManager = SettingManager.getInstance(context);
    }

    public void updateTopTracks(ArrayList<Album> albums) {
        this.albums.clear();
        this.albums.addAll(albums);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClickListener(View view, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_album, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.txtAlbum.setText(albums.get(position).getTitle());
        holder.txtArtist.setText("Various Artists");

        handleCoverPlaceholderColor(holder.imgCover, position);

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
            Glide.with(context).load(albums.get(position).getCover()).placeholder(R.color.colorLight5).into(imgCover);
        } else {
            Glide.with(context).load(albums.get(position).getCover()).placeholder(R.color.colorDark5).into(imgCover);
        }
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private RoundImageView imgCover;
        private TextView txtAlbum;
        private TextView txtArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgCover = itemView.findViewById(R.id.imgCover);
            txtAlbum = itemView.findViewById(R.id.txtAlbum);
            txtArtist = itemView.findViewById(R.id.txtArtist);
        }
    }
}

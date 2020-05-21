package com.albertkhang.tunedaily.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.activities.PlaylistActivity;
import com.albertkhang.tunedaily.utils.Playlist;
import com.albertkhang.tunedaily.utils.PlaylistManager;
import com.albertkhang.tunedaily.views.RoundImageView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private ArrayList<Playlist> playlists = new ArrayList<>();
    private Context context;
    private PlaylistManager playlistManager;

    public PlaylistAdapter(Context context) {
        this.context = context;
        this.playlistManager = PlaylistManager.getInstance(context);
    }

    public void update(ArrayList<Playlist> playlists) {
        this.playlists.clear();
        this.playlists.addAll(playlists);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_playlist, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistAdapter.ViewHolder holder, final int position) {
        Log.d("PlaylistAdapter", playlists.get(position).getName() + ": " + playlists.get(position));

        String cover = playlistManager.getPlaylistCover(playlists.get(position).getName());
        if (!cover.equals("")) {
            Glide.with(context).load(cover).placeholder(R.drawable.ic_playlist_cover).into(holder.imgCover);
        }
        handleAddToPlaylistListener(holder.imgCover);

        holder.txtPlaylist.setText(playlists.get(position).getName());
        holder.txtTotal.setText(String.valueOf(playlists.get(position).getTotal()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlaylistActivity.class);
                intent.putExtra("type", PlaylistActivity.TYPE.PLAYLIST);
                intent.putExtra("name", playlists.get(position).getName());
                context.startActivity(intent);
            }
        });
    }

    private void handleAddToPlaylistListener(final RoundImageView view) {
        playlistManager.setOnCompletePlaylistCoverListener(new PlaylistManager.OnCompletePlaylistCoverListener() {
            @Override
            public void onCompletePlaylistCoverListener(String cover) {
                Glide.with(context).load(cover).placeholder(view.getDrawable()).into(view);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private RoundImageView imgCover;
        private TextView txtPlaylist;
        private TextView txtTotal;
        private ImageView imgMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgCover = itemView.findViewById(R.id.imgCover);
            txtPlaylist = itemView.findViewById(R.id.txtPlaylist);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            imgMore = itemView.findViewById(R.id.imgMore);
        }
    }
}

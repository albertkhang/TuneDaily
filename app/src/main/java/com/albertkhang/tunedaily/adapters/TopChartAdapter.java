package com.albertkhang.tunedaily.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.Track;
import com.albertkhang.tunedaily.views.RoundImageView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class TopChartAdapter extends RecyclerView.Adapter<TopChartAdapter.ViewHolder> {
    private ArrayList<Track> tracks = new ArrayList<>();
    private Context context;
    private SettingManager settingManager;

    public TopChartAdapter(Context context) {
        this.context = context;
        this.settingManager = new SettingManager(context);
    }

    public void updateTopTracks(ArrayList<Track> tracks) {
        this.tracks.clear();
        this.tracks.addAll(tracks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_top_chart, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        handlePositionTextColor(holder.txtPosition, position + 1);
        holder.txtSongName.setText(tracks.get(position).getTitle());
        holder.txtSingerName.setText(tracks.get(position).getArtist());

        handleCoverPlaceholderColor(holder.imgCover, position);
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
                break;

            case 2:
                txtPosition.setTextColor(context.getResources().getColor(R.color.colorSt2));
                break;

            case 3:
                txtPosition.setTextColor(context.getResources().getColor(R.color.colorSt3));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtPosition;
        private RoundImageView imgCover;
        private TextView txtSongName;
        private TextView txtSingerName;
        private ImageView imgMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtPosition = itemView.findViewById(R.id.txtPosition);
            imgCover = itemView.findViewById(R.id.imgCover);
            txtSongName = itemView.findViewById(R.id.txtSongName);
            txtSingerName = itemView.findViewById(R.id.txtSingerName);
            imgMore = itemView.findViewById(R.id.imgMore);
        }
    }
}

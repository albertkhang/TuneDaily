package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.adapters.LibraryPlaylistAdapter;
import com.albertkhang.tunedaily.models.Playlist;
import com.albertkhang.tunedaily.utils.PlaylistManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SelectPlaylistToAddActivity extends AppCompatActivity {
    private static final String LOG_TAG = "SelectPlaylistActivity";

    private ImageView imgBack;
    private RecyclerView recycler_view;
    private ArrayList<String> playlistNames;
    private LibraryPlaylistAdapter playlistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_playlist_to_add);

        addControl();
        addEvent();
    }

    private void addControl() {
        imgBack = findViewById(R.id.imgBack);
        recycler_view = findViewById(R.id.recycler_view);

        playlistNames = (ArrayList<String>) PlaylistManager.getInstance().getAllPlaylist();
        playlistAdapter = new LibraryPlaylistAdapter(this);
        recycler_view.setAdapter(playlistAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.VERTICAL);
        recycler_view.setLayoutManager(manager);

        ArrayList<Playlist> playlists = new ArrayList<>();

        for (int i = 0; i < playlistNames.size(); i++) {
            String title = playlistNames.get(i);
            String cover = PlaylistManager.getInstance().getPlaylistCover(title);
            List<Integer> tracks = PlaylistManager.getInstance().getPlaylistTracks(title);

            playlists.add(new Playlist(-1, title, cover, tracks));
        }

        playlistAdapter.update(playlists);
        playlistAdapter.hidePlaylistMore();
//        Log.d("playlisttest", "playlists: " + playlists.toString());
    }

    private void addEvent() {
        playlistAdapter.setOnAddLibraryItemClickListener(new LibraryPlaylistAdapter.OnAddLibraryItemClickListener() {
            @Override
            public void onAddLibraryItemClickListener(View view, int position) {
                int id = getIntent().getIntExtra("id", -1);
                String songName = getIntent().getStringExtra("title");
//                Log.d("playlisttest", "title: " + songName + ", id: " + id);

                String playlistname = playlistNames.get(position);

                if (songName != null) {
                    ArrayList list = PlaylistManager.getInstance().getPlaylistTracks(playlistname);
                    if (list != null) {
//                        Log.d("playlisttest", "run");
                        if (!PlaylistManager.getInstance().isContainIPlaylist(id, playlistname)) {
                            PlaylistManager.getInstance().addToFirstPlaylist(playlistname, id);
                            Toast.makeText(SelectPlaylistToAddActivity.this, "Added \"" + songName + "\" into \"" + playlistNames.get(position) + "\"", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SelectPlaylistToAddActivity.this, "\"" + songName + "\" existed into \"" + playlistname + "\"", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                finish();
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
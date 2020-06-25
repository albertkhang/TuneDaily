package com.albertkhang.tunedaily.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.activities.PlaylistActivity;
import com.albertkhang.tunedaily.adapters.LibraryPlaylistAdapter;
import com.albertkhang.tunedaily.events.UpdateLanguageEvent;
import com.albertkhang.tunedaily.events.UpdatePlaylistLibraryEvent;
import com.albertkhang.tunedaily.models.Playlist;
import com.albertkhang.tunedaily.utils.PlaylistManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.SoftKeyboardManager;
import com.albertkhang.tunedaily.events.UpdateThemeEvent;
import com.albertkhang.tunedaily.views.RoundImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LibraryFragment extends Fragment {
    private static final String LOG_TAG = "LibraryFragment";

    private SettingManager settingManager;
    private ConstraintLayout liked_songs_frame;
    private LinearLayout root_view;
    private RelativeLayout top_frame;
    private TextView txtLibrary;
    private TextView txtLikedSongs;
    private TextView txtTotal;
    private TextView txtSongsAmount;
    private ImageView imgMusicNote;
    private RoundImageView playlist_background_frame;
    private TextView txtPlaylist;
    private ConstraintLayout create_new_playlist_frame;

    private PlaylistManager playlistManager;

    private RecyclerView rvPlaylist;
    private LibraryPlaylistAdapter libraryPlaylistAdapter;
    private ArrayList<Playlist> playlists;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addControl(view);
        addEvent();
    }

    private void addControl(View view) {
        settingManager = SettingManager.getInstance(getContext());

        liked_songs_frame = view.findViewById(R.id.liked_songs_frame);
        root_view = view.findViewById(R.id.root_view);
        top_frame = view.findViewById(R.id.top_frame);
        txtLibrary = view.findViewById(R.id.txtLibrary);

        txtLikedSongs = view.findViewById(R.id.txtLikedSongs);
        txtTotal = view.findViewById(R.id.txtTotal);
        txtSongsAmount = view.findViewById(R.id.txtSongsAmount);
        imgMusicNote = view.findViewById(R.id.imgMusicNote);

        playlist_background_frame = view.findViewById(R.id.playlist_background_frame);
        txtPlaylist = view.findViewById(R.id.txtPlaylist);

        create_new_playlist_frame = view.findViewById(R.id.create_new_playlist_frame);

        updateTheme();

        playlistManager = PlaylistManager.getInstance();

        rvPlaylist = view.findViewById(R.id.rvPlaylist);
        libraryPlaylistAdapter = new LibraryPlaylistAdapter(getContext());
        rvPlaylist.setAdapter(libraryPlaylistAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rvPlaylist.setLayoutManager(linearLayoutManager);

        libraryPlaylistAdapter.setOnMoreListener(new LibraryPlaylistAdapter.OnMoreListener() {
            @Override
            public void onMoreListener(View view, int position) {
                showMoreItem(playlists.get(position));
            }
        });

        List<String> allpplaylists = playlistManager.getAllPlaylist();
        Log.d(LOG_TAG, "playlist size: " + allpplaylists.size());

        for (int i = 0; i < allpplaylists.size(); i++) {
            Log.d(LOG_TAG, allpplaylists.get(i));
        }
    }

    private void showMoreItem(Playlist playlist) {
        PlaylistMoreFragment playlistMoreFragment = new PlaylistMoreFragment(playlist);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isShowDeletePlaylist", true);
        playlistMoreFragment.setArguments(bundle);
        playlistMoreFragment.show(requireActivity().getSupportFragmentManager(), "PlaylistMoreFragment");
    }

    private void addEvent() {
        liked_songs_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PlaylistActivity.class);
                ArrayList<Integer> ids = (ArrayList<Integer>) PlaylistManager.getInstance().getLikedSongsIds();
                Bundle bundle = new Bundle();
                bundle.putIntegerArrayList("ids", ids);
                intent.putExtra("ids", bundle);
                intent.putExtra("title", getString(R.string.liked_songs));
                startActivity(intent);
            }
        });

        create_new_playlist_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void showDialog() {
        final Dialog dialog = new Dialog(getContext(), R.style.RoundCornerDialogFragment);
        dialog.setContentView(R.layout.fragment_create_new_playlist);

        final EditText txtPlaylistName = dialog.findViewById(R.id.txtPlaylistName);
        TextView txtTitle = dialog.findViewById(R.id.txtTitle);

        Button cancel = dialog.findViewById(R.id.btnCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoftKeyboardManager.hideSoftKeyboard(getActivity(), txtPlaylistName);
                //TODO: Remove comment to test add track to "albert" playlist
//                ArrayList list = playlistManager.getPlaylistTracks("albert");
//                if (list != null) {
//                    Log.d("cancel", "run");
//                    playlistManager.addToFirstPlaylist("albert", (new Random()).nextInt(100 + 1));
//                    updatePlaylist();
//                }

                dialog.cancel();
            }
        });

        Button create = dialog.findViewById(R.id.btnCreate);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (playlistManager.isPlaylistNameValidated(txtPlaylistName.getText().toString())) {
                    case PlaylistManager.NAME.EMPTY:
                        txtPlaylistName.setError("Playlist name must not empty.");
                        break;

                    case PlaylistManager.NAME.NOT_VALID:
                        txtPlaylistName.setError("Only letters a-z, A-Z, 0-9, space.\nFirst letter must not a space.");
                        break;

                    case PlaylistManager.NAME.VALID:
                        SoftKeyboardManager.hideSoftKeyboard(getActivity(), txtPlaylistName);

                        playlistManager.createNewPlaylist(txtPlaylistName.getText().toString());

                        Toast.makeText(getContext(), "Created " + txtPlaylistName.getText(), Toast.LENGTH_LONG).show();
                        updatePlaylist();

                        dialog.dismiss();
                        break;

                    case PlaylistManager.NAME.EXIST:
                        txtPlaylistName.setError("Name exist.");
                        break;
                }
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Log.d("dialog", "onShow");
                SoftKeyboardManager.showSoftKeyboard(getActivity(), txtPlaylistName);
            }
        });

        dialog.setCanceledOnTouchOutside(false);

        if (settingManager.isDarkTheme()) {
            dialog.findViewById(R.id.root_view).setBackgroundResource(R.drawable.round_dark_dialog_background);
            txtTitle.setTextColor(getResources().getColor(R.color.colorLight1));

            txtPlaylistName.setTextColor(getResources().getColor(R.color.colorLight1));
            txtPlaylistName.setHintTextColor(getResources().getColor(R.color.colorLight5));

            cancel.setBackgroundResource(R.drawable.round_dark_dialog_button);
            create.setBackgroundResource(R.drawable.round_dark_dialog_button);

            cancel.setTextColor(getResources().getColor(R.color.colorLight1));
            create.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            dialog.findViewById(R.id.root_view).setBackgroundResource(R.drawable.round_light_dialog_background);
            txtTitle.setTextColor(getResources().getColor(R.color.colorDark1));

            txtPlaylistName.setTextColor(getResources().getColor(R.color.colorDark1));
            txtPlaylistName.setHintTextColor(getResources().getColor(R.color.colorDark5));

            cancel.setBackgroundResource(R.drawable.round_light_dialog_button);
            create.setBackgroundResource(R.drawable.round_light_dialog_button);

            cancel.setTextColor(getResources().getColor(R.color.colorDark1));
            create.setTextColor(getResources().getColor(R.color.colorDark1));
        }

        dialog.show();
    }

    private void updatePlaylist() {
        List<String> allPlaylist = playlistManager.getAllPlaylist();
        Log.d("getAllPlayList", "size: " + allPlaylist.size());

        playlists = new ArrayList<>();
        for (int i = 0; i < allPlaylist.size(); i++) {
            String title = allPlaylist.get(i);
            String cover = playlistManager.getPlaylistCover(allPlaylist.get(i));
            ArrayList<Integer> tracks = playlistManager.getPlaylistTracks(allPlaylist.get(i));

//            Log.d(LOG_TAG, "[" + i + "] title: " + title);
//            Log.d(LOG_TAG, "[" + i + "] cover: " + cover);
//            Log.d(LOG_TAG, "[" + i + "] tracks: " + tracks);

            playlists.add(new Playlist(-1, title, cover, tracks));
        }

        libraryPlaylistAdapter.update(playlists);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTheme();
        txtTotal.setText(String.valueOf(PlaylistManager.getInstance().getLikedSongsSize()));
        updatePlaylist();
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));
            top_frame.setBackgroundColor(getResources().getColor(R.color.colorDark2));

            txtLibrary.setTextColor(getResources().getColor(R.color.colorLight1));

            txtLikedSongs.setTextColor(getResources().getColor(R.color.colorLight1));
            txtTotal.setTextColor(getResources().getColor(R.color.colorLight5));
            txtSongsAmount.setTextColor(getResources().getColor(R.color.colorLight5));

            imgMusicNote.setColorFilter(getResources().getColor(R.color.colorLight3));

            playlist_background_frame.setColorFilter(getResources().getColor(R.color.colorDark3));

            txtPlaylist.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));
            top_frame.setBackgroundColor(getResources().getColor(R.color.colorLight2));

            txtLibrary.setTextColor(getResources().getColor(R.color.colorDark1));

            txtLikedSongs.setTextColor(getResources().getColor(R.color.colorDark1));
            txtSongsAmount.setTextColor(getResources().getColor(R.color.colorDark5));
            txtTotal.setTextColor(getResources().getColor(R.color.colorDark5));

            imgMusicNote.setColorFilter(getResources().getColor(R.color.colorDark3));
            playlist_background_frame.setColorFilter(getResources().getColor(R.color.colorLight3));

            txtPlaylist.setTextColor(getResources().getColor(R.color.colorDark1));
        }
    }

    private void updateLanguage() {
        settingManager.updateLanguageConfiguration();

        txtLibrary.setText(getString(R.string.library_header));
        txtLikedSongs.setText(getString(R.string.liked_songs));
        txtSongsAmount.setText(getString(R.string.songs_library));
        txtPlaylist.setText(getString(R.string.create_new_playlist));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdatePlaylistLibraryEvent updatePlaylistLibraryEvent) {
        updatePlaylist();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateThemeEvent updateThemeEvent) {
        updateTheme();
        libraryPlaylistAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateLanguageEvent updateLanguageEvent) {
        updateLanguage();
    }
}

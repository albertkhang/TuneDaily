package com.albertkhang.tunedaily.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.activities.PlaylistActivity;
import com.albertkhang.tunedaily.utils.PlaylistManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.SoftKeyboardManager;
import com.albertkhang.tunedaily.utils.UpdateThemeEvent;
import com.albertkhang.tunedaily.views.RoundImageView;
import com.google.firebase.firestore.model.Values;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

public class LibraryFragment extends Fragment {

    private SettingManager settingManager;
    private ConstraintLayout liked_songs_frame;
    private LinearLayout root_view;
    private RelativeLayout top_frame;
    private TextView txtLibrary;
    private TextView txtLikedSongs;
    private TextView txtSongsAmount;
    private ImageView imgMusicNote;
    private RoundImageView playlist_background_frame;
    private TextView txtPlaylist;
    private ConstraintLayout create_new_playlist_frame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
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
        txtSongsAmount = view.findViewById(R.id.txtSongsAmount);
        imgMusicNote = view.findViewById(R.id.imgMusicNote);

        playlist_background_frame = view.findViewById(R.id.playlist_background_frame);
        txtPlaylist = view.findViewById(R.id.txtPlaylist);

        create_new_playlist_frame = view.findViewById(R.id.create_new_playlist_frame);

        updateTheme();
    }

    private void addEvent() {
        liked_songs_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PlaylistActivity.class);
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
        final Dialog dialog = new Dialog(getContext(), R.style.RoundCornerDialogFrament);
        dialog.setContentView(R.layout.fragment_create_new_playlist);

        Button cancel = dialog.findViewById(R.id.btnCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        final EditText txtPlaylistName = dialog.findViewById(R.id.txtPlaylistName);
        TextView txtTitle = dialog.findViewById(R.id.txtTitle);

        Button create = dialog.findViewById(R.id.btnCreate);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (PlaylistManager.isPlaylistNameValidated(txtPlaylistName.getText().toString())) {
                    case PlaylistManager.NAME_EMPTY:
                        txtPlaylistName.setError("Playlist name must not empty.");
                        break;

                    case PlaylistManager.NAME_NOT_VALID:
                        txtPlaylistName.setError("Only letters a-z, A-Z, 0-9, space.\nFirst letter must not a space.");
                        break;

                    case PlaylistManager.NAME_VALID:
                        Toast.makeText(getContext(), "Created " + txtPlaylistName.getText(), Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        break;
                }
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Log.d("dialog", "onShow");

                if (txtPlaylistName.requestFocus()) {
                    Log.d("dialog", "requestFocus");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            txtPlaylistName.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    SoftKeyboardManager.showSoftKeyboard(getContext(), txtPlaylistName);
                                }
                            }, 100);
                        }
                    }).start();
                }
            }
        });

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

    @Override
    public void onResume() {
        super.onResume();
        updateTheme();
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));
            top_frame.setBackgroundColor(getResources().getColor(R.color.colorDark2));

            txtLibrary.setTextColor(getResources().getColor(R.color.colorLight1));

            txtLikedSongs.setTextColor(getResources().getColor(R.color.colorLight1));
            txtSongsAmount.setTextColor(getResources().getColor(R.color.colorLight3));

            imgMusicNote.setColorFilter(getResources().getColor(R.color.colorLight3));

            playlist_background_frame.setColorFilter(getResources().getColor(R.color.colorDark3));

            txtPlaylist.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));
            top_frame.setBackgroundColor(getResources().getColor(R.color.colorLight2));

            txtLibrary.setTextColor(getResources().getColor(R.color.colorDark1));

            txtLikedSongs.setTextColor(getResources().getColor(R.color.colorDark1));
            txtSongsAmount.setTextColor(getResources().getColor(R.color.colorDark3));

            imgMusicNote.setColorFilter(getResources().getColor(R.color.colorDark3));
            playlist_background_frame.setColorFilter(getResources().getColor(R.color.colorLight3));

            txtPlaylist.setTextColor(getResources().getColor(R.color.colorDark1));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateThemeEvent(UpdateThemeEvent updateThemeEvent) {
        updateTheme();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}

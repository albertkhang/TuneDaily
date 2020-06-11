package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.adapters.PlaylistAdapter;
import com.albertkhang.tunedaily.adapters.TrackAdapter;
import com.albertkhang.tunedaily.events.UpdateDownloadedTrack;
import com.albertkhang.tunedaily.fragments.TrackMoreFragment;
import com.albertkhang.tunedaily.models.Playlist;
import com.albertkhang.tunedaily.utils.FirebaseManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.SoftKeyboardManager;
import com.albertkhang.tunedaily.models.Track;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class InSearchActivity extends AppCompatActivity {
    private SettingManager settingManager;

    private EditText txtSearchText;
    private ScrollView scroll_view;
    private ImageView imgCollapse;
    private RelativeLayout root_view;
    private ConstraintLayout top_frame;
    private ImageView imgClear;
    private TextView txtArtist;
    private TextView txtSongs;
    private TextView txtResultStatus;
    private FrameLayout shimmer_artists;
    private FrameLayout shimmer_songs;
    private RecyclerView rvArtist;
    private RecyclerView rvTracks;
    private LinearLayout artist_frame;
    private LinearLayout songs_frame;

    private PlaylistAdapter artistsAdapter;
    private TrackAdapter tracksAdapter;

    private static final long DELAY_GETTING_TEXT_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_search);

        addControl();
        addEvent();
    }

    private void addControl() {
        settingManager = SettingManager.getInstance(this);

        txtSearchText = findViewById(R.id.txtSearchText);
        txtSearchText.requestFocus();
        scroll_view = findViewById(R.id.scroll_view);
        imgCollapse = findViewById(R.id.imgCollapse);
        root_view = findViewById(R.id.root_view);
        top_frame = findViewById(R.id.top_frame);
        imgClear = findViewById(R.id.imgClear);
        txtArtist = findViewById(R.id.txtArtist);
        txtSongs = findViewById(R.id.txtSongs);
        rvArtist = findViewById(R.id.rvArtist);
        rvTracks = findViewById(R.id.rvTracks);
        artist_frame = findViewById(R.id.artist_frame);
        songs_frame = findViewById(R.id.songs_frame);

        artistsAdapter = new PlaylistAdapter(this);
        rvArtist.setAdapter(artistsAdapter);

        tracksAdapter = new TrackAdapter(this);
        rvTracks.setAdapter(tracksAdapter);

        LinearLayoutManager artistManager = new LinearLayoutManager(this);
        artistManager.setOrientation(RecyclerView.HORIZONTAL);
        rvArtist.setLayoutManager(artistManager);

        LinearLayoutManager tracksManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        tracksManager.setOrientation(RecyclerView.VERTICAL);
        rvTracks.setLayoutManager(tracksManager);

        txtResultStatus = findViewById(R.id.txtResultStatus);
        shimmer_artists = findViewById(R.id.shimmer_artists);
        shimmer_songs = findViewById(R.id.shimmer_songs);

        updateTheme();
    }

    private void showMoreItem(Track track) {
        TrackMoreFragment trackMoreFragment = new TrackMoreFragment(track);
        trackMoreFragment.show(getSupportFragmentManager(), "FragmentTrackMore");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addEvent() {
        scroll_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SoftKeyboardManager.hideSoftKeyboard(InSearchActivity.this, txtSearchText);
                txtSearchText.clearFocus();
                scroll_view.requestFocus();
                return false;
            }
        });

        imgCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        txtSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
//                Log.d("txtSearchText", "beforeTextChanged: " + "start: " + start + ", count:" + count + ", after: " + after);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
//                Log.d("txtSearchText", "onTextChanged: " + "start: " + start + ", before: " + before + ", count: " + count);
            }

            @Override
            public void afterTextChanged(final Editable editable) {
//                Log.d("txtSearchText", "afterTextChanged: " + "editable: " + editable);
                if (editable.length() != 0) {
                    imgClear.setVisibility(View.VISIBLE);
                } else {
                    imgClear.setVisibility(View.GONE);
                }

                final String temp = editable.toString().toLowerCase();

                Runnable r = new Runnable() {
                    public void run() {
//                        Log.d("afterTextChanged", "editable: " + editable + ", temp: " + temp);
                        if (temp.equals(editable.toString().toLowerCase()) && !temp.equals("")) {
                            Log.d("afterTextChanged", "run");

                            txtResultStatus.setVisibility(View.GONE);
                            scroll_view.setVisibility(View.VISIBLE);

                            artist_frame.setVisibility(View.VISIBLE);
                            songs_frame.setVisibility(View.VISIBLE);

                            shimmer_artists.setVisibility(View.VISIBLE);
                            shimmer_songs.setVisibility(View.VISIBLE);

                            FirebaseManager.getInstance().setReadByTitleListener(new FirebaseManager.ReadByTitleListener() {
                                @Override
                                public void readTrackByTitleListener(final ArrayList<Track> tracks) {
                                    Log.d("afterTextChanged", "track: " + tracks.size());

                                    if (tracks.size() == 0) {
                                        songs_frame.setVisibility(View.GONE);
                                    } else {
                                        shimmer_songs.setVisibility(View.GONE);
                                        rvTracks.setVisibility(View.VISIBLE);
                                        tracksAdapter.update(tracks);
                                    }

                                    tracksAdapter.setOnMoreListener(new TrackAdapter.OnMoreListener() {
                                        @Override
                                        public void onMoreListener(View view, int position) {
                                            showMoreItem(tracks.get(position));
                                        }
                                    });
                                }

                                @Override
                                public void readAlbumByTitleListener(final ArrayList<Playlist> albums) {
                                    Log.d("afterTextChanged", "albums: " + albums.size());

                                    if (albums.size() == 0) {
                                        artist_frame.setVisibility(View.GONE);
                                    } else {
                                        shimmer_artists.setVisibility(View.GONE);
                                        rvArtist.setVisibility(View.VISIBLE);
                                        artistsAdapter.update(albums);
                                    }
                                }

                                @Override
                                public void handleSearchIsEmptyListener() {
                                    Log.d("afterTextChanged", "handleSearchIsEmptyListener");

                                    scroll_view.setVisibility(View.GONE);
                                    txtResultStatus.setVisibility(View.VISIBLE);

                                    txtResultStatus.setText(getString(R.string.found_0_result));
                                }
                            });

                            FirebaseManager.getInstance().searchTrackByTitle(temp);
                            FirebaseManager.getInstance().searchAlbumByTitle(temp);
                        } else {
                            if (temp.equals("")) {
                                txtResultStatus.setVisibility(View.VISIBLE);
                                scroll_view.setVisibility(View.GONE);

                                txtResultStatus.setText(getString(R.string.your_results_will_be_shown_here));
                            }
                        }
                    }
                };

                Handler handler = new Handler();
                handler.postDelayed(r, DELAY_GETTING_TEXT_DURATION);
            }
        });

        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtSearchText.setText("");
                txtResultStatus.setText(getString(R.string.your_results_will_be_shown_here));
            }
        });
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorDark2));
            imgCollapse.setColorFilter(getResources().getColor(R.color.colorLight5));
            txtSearchText.setTextColor(getResources().getColor(R.color.colorLight1));
            imgClear.setColorFilter(getResources().getColor(R.color.colorLight5));

            txtArtist.setTextColor(getResources().getColor(R.color.colorLight1));
            txtSongs.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorLight2));
            imgCollapse.setColorFilter(getResources().getColor(R.color.colorDark5));
            txtSearchText.setTextColor(getResources().getColor(R.color.colorDark1));
            imgClear.setColorFilter(getResources().getColor(R.color.colorDark5));

            txtArtist.setTextColor(getResources().getColor(R.color.colorDark1));
            txtSongs.setTextColor(getResources().getColor(R.color.colorDark1));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateDownloadedTrack updateDownloadedTrack) {
        tracksAdapter.update();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}

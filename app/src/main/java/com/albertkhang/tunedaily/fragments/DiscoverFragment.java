package com.albertkhang.tunedaily.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.activities.SettingsActivity;
import com.albertkhang.tunedaily.adapters.PlaylistAdapter;
import com.albertkhang.tunedaily.adapters.TopChartAdapter;
import com.albertkhang.tunedaily.events.UpdateDownloadedTrack;
import com.albertkhang.tunedaily.events.UpdateLanguageEvent;
import com.albertkhang.tunedaily.models.Playlist;
import com.albertkhang.tunedaily.utils.FirebaseManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.models.TopTrack;
import com.albertkhang.tunedaily.models.Track;
import com.albertkhang.tunedaily.events.UpdateThemeEvent;
import com.albertkhang.tunedaily.views.CircleImageView;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DiscoverFragment extends Fragment {
    private ImageView imgSettings;
    private CircleImageView imgUser;
    private CircleImageView imgCover;
    private ShimmerFrameLayout shimmer_top_chart;
    private ShimmerFrameLayout shimmer_popular_albums;
    private ShimmerFrameLayout shimmer_best_of_artists;
    private RecyclerView rvTopChart;
    private RecyclerView rvPopularAlbums;
    private RecyclerView rvBestOfArtists;
    private TopChartAdapter topChartAdapter;
    private PlaylistAdapter popularPlaylistAdapter;
    private PlaylistAdapter bestOfArtistPlaylistAdapter;
    private LinearLayout root_view;
    private RelativeLayout top_frame;
    private TextView txtTopChart;
    private TextView txtPopularAlbum;
    private TextView txtBestOfArtist;
    private ImageView imgTopLogo;

    private SwipeRefreshLayout swipe_frame;

    private SettingManager settingManager = SettingManager.getInstance(getContext());

    private static final String LOG_TAG = "DiscoverAuthentication";
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        return inflater.inflate(R.layout.fragment_discover, container, false);
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
        addEvent(view);
    }

    private void addControl(View view) {
        imgSettings = view.findViewById(R.id.imgSettings);
        imgUser = view.findViewById(R.id.imgUser);
        imgCover = view.findViewById(R.id.imgCover);
        root_view = view.findViewById(R.id.root_view);
        top_frame = view.findViewById(R.id.top_frame);
        txtTopChart = view.findViewById(R.id.txtTopChart);
        txtPopularAlbum = view.findViewById(R.id.txtPopularAlbum);
        txtBestOfArtist = view.findViewById(R.id.txtBestOfArtist);
        imgTopLogo = view.findViewById(R.id.imgTopLogo);

        swipe_frame = view.findViewById(R.id.swipe_frame);

        shimmer_top_chart = view.findViewById(R.id.shimmer_top_chart);
        shimmer_popular_albums = view.findViewById(R.id.shimmer_popular_albums);
        shimmer_best_of_artists = view.findViewById(R.id.shimmer_best_of_artists);
        rvTopChart = view.findViewById(R.id.rvTopChart);
        rvPopularAlbums = view.findViewById(R.id.rvPopularAlbums);
        rvBestOfArtists = view.findViewById(R.id.rvBestOfArtists);

        //TopChart
        topChartAdapter = new TopChartAdapter(getContext());
        rvTopChart.setAdapter(topChartAdapter);

        LinearLayoutManager managerTopChart = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        managerTopChart.setOrientation(RecyclerView.VERTICAL);
        rvTopChart.setLayoutManager(managerTopChart);

        //Popular Albums
        popularPlaylistAdapter = new PlaylistAdapter(getContext());
        rvPopularAlbums.setAdapter(popularPlaylistAdapter);

        LinearLayoutManager managerAlbums = new LinearLayoutManager(getContext());
        managerAlbums.setOrientation(RecyclerView.HORIZONTAL);
        rvPopularAlbums.setLayoutManager(managerAlbums);

        //BestOfArtists
        bestOfArtistPlaylistAdapter = new PlaylistAdapter(getContext());
        rvBestOfArtists.setAdapter(bestOfArtistPlaylistAdapter);

        LinearLayoutManager managerArtist = new LinearLayoutManager(getContext());
        managerArtist.setOrientation(RecyclerView.HORIZONTAL);
        rvBestOfArtists.setLayoutManager(managerArtist);

        mAuth = FirebaseAuth.getInstance();
        configureGoogleSignIn();

        updateTheme();

        swipe_frame.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateList();
                swipe_frame.setRefreshing(false);
            }
        });

        updateList();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(LOG_TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(LOG_TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(LOG_TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(LOG_TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Log.d(LOG_TAG, "uid: " + user.getUid());
            Log.d(LOG_TAG, "email: " + user.getEmail());
            Log.d(LOG_TAG, "name: " + user.getDisplayName());
            Log.d(LOG_TAG, "photo: " + user.getPhotoUrl());

            Log.d(LOG_TAG, "creationTimestamp: " + getDate(user.getMetadata().getCreationTimestamp()));
            Log.d(LOG_TAG, "lastSignInTimestamp: " + getDate(user.getMetadata().getLastSignInTimestamp()));

            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .centerCrop()
                    .into(imgCover);

            if (getDate(user.getMetadata().getCreationTimestamp()).equals(getDate(user.getMetadata().getLastSignInTimestamp()))) {
                FirebaseManager.getInstance().createDefaultUserSetting(user.getUid());
            }
        } else {
            Log.d(LOG_TAG, "user == null");
            imgCover.setImageResource(0);
        }
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        return DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();
    }

    private void configureGoogleSignIn() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
    }

    private void updateList() {
        updateTopChart();
        updatePopularAlbum();
        rvPopularAlbums.scrollToPosition(0);
        updateBestOfArtist();
        rvBestOfArtists.scrollToPosition(0);
    }

    private void updateBestOfArtist() {
        shimmer_best_of_artists.setVisibility(View.VISIBLE);
        rvBestOfArtists.setVisibility(View.GONE);

        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        firebaseManager.setReadBestOfArtistListener(new FirebaseManager.ReadBestOfArtistListener() {
            @Override
            public void readBestOfArtistListener(final ArrayList<Playlist> playlists) {
                shimmer_best_of_artists.setVisibility(View.GONE);
                rvBestOfArtists.setVisibility(View.VISIBLE);

                bestOfArtistPlaylistAdapter.update(playlists);
            }
        });

        firebaseManager.getBestOfArtist();
    }

    private void updatePopularAlbum() {
        shimmer_popular_albums.setVisibility(View.VISIBLE);
        rvPopularAlbums.setVisibility(View.GONE);

        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        firebaseManager.setReadPopularAlbumsListener(new FirebaseManager.ReadPopularAlbumsListener() {
            @Override
            public void readPopularAlbumsListener(final ArrayList<Playlist> playlists) {

                shimmer_popular_albums.setVisibility(View.GONE);
                rvPopularAlbums.setVisibility(View.VISIBLE);

                popularPlaylistAdapter.update(playlists);
            }
        });

        firebaseManager.getPopularAlbums();
    }

    private void updateTopChart() {
        shimmer_top_chart.setVisibility(View.VISIBLE);
        rvTopChart.setVisibility(View.GONE);

        final ArrayList ids = new ArrayList();

        final FirebaseManager firebaseManager = FirebaseManager.getInstance();
        firebaseManager.setReadTopTrackIdsListener(new FirebaseManager.ReadTopTrackIdsListener() {
            @Override
            public void updateTopTrackListener(ArrayList<TopTrack> topChartIdsOrdered) {
                for (TopTrack track : topChartIdsOrdered) {
                    ids.add(track.getId());
                }

                firebaseManager.getTrackFromIds(ids);
                Log.d("firebaseManager", "ids: " + ids.toString());
            }
        });

        firebaseManager.setReadTrackFromIdsListener(new FirebaseManager.ReadTrackFromIdsListener() {
            @Override
            public void readTrackFromIdsListener(final ArrayList<Track> tracks) {
                for (Track track : tracks) {
                    Log.d("firebaseManager", track.toString());
                }

                topChartAdapter.setOnMoreListener(new TopChartAdapter.OnMoreListener() {
                    @Override
                    public void onMoreListener(View view, int position) {
                        showMoreItem(tracks.get(position));
                    }
                });

                shimmer_top_chart.setVisibility(View.GONE);
                rvTopChart.setVisibility(View.VISIBLE);

                topChartAdapter.update(tracks);
            }
        });

        firebaseManager.getTopTrack();
    }

    private void updateTheme() {
        SettingManager manager = SettingManager.getInstance(getContext());
        if (manager.isDarkTheme()) {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorDark2));
            imgTopLogo.setImageResource(R.drawable.ic_logo_and_text_dark);
            imgUser.setColorFilter(getResources().getColor(R.color.colorLight5));
            imgSettings.setColorFilter(getResources().getColor(R.color.colorLight5));

            txtTopChart.setTextColor(getResources().getColor(R.color.colorLight1));
            txtPopularAlbum.setTextColor(getResources().getColor(R.color.colorLight1));
            txtBestOfArtist.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorLight2));
            imgTopLogo.setImageResource(R.drawable.ic_logo_and_text_light);
            imgUser.setColorFilter(getResources().getColor(R.color.colorDark5));
            imgSettings.setColorFilter(getResources().getColor(R.color.colorDark5));

            txtTopChart.setTextColor(getResources().getColor(R.color.colorDark1));
            txtPopularAlbum.setTextColor(getResources().getColor(R.color.colorDark1));
            txtBestOfArtist.setTextColor(getResources().getColor(R.color.colorDark1));
        }

        topChartAdapter.notifyDataSetChanged();
        popularPlaylistAdapter.notifyDataSetChanged();
        bestOfArtistPlaylistAdapter.notifyDataSetChanged();
    }

    private void updateLanguage() {
        settingManager.updateLanguageConfiguration();

        txtTopChart.setText(getString(R.string.top_chart));
        txtPopularAlbum.setText(getString(R.string.popular_albums));
        txtBestOfArtist.setText(getString(R.string.best_of_artists));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateThemeEvent updateThemeEvent) {
        updateTheme();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateLanguageEvent updateLanguageEvent) {
        Log.d("DiscoverFragment", "UpdateLanguageEvent");
        updateLanguage();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateDownloadedTrack updateDownloadedTrack) {
        Log.d("DiscoverFragment", "UpdateDownloadedTrack");
        topChartAdapter.update();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTheme();
        topChartAdapter.update();
    }

    private void addEvent(View view) {
        imgSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.animation_start_enter, R.anim.animation_start_leave);
            }
        });

        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    signIn();
                }
            }
        });
    }

    private void showMoreItem(Track track) {
        TrackMoreFragment trackMoreFragment = new TrackMoreFragment(track);
        trackMoreFragment.show(requireActivity().getSupportFragmentManager(), "FragmentTrackMore");
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
}
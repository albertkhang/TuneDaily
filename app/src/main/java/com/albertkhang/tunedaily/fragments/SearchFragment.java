package com.albertkhang.tunedaily.fragments;

        import android.content.Intent;
        import android.graphics.drawable.Drawable;
        import android.os.Bundle;

        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.fragment.app.Fragment;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.FrameLayout;
        import android.widget.ImageView;
        import android.widget.TextView;

        import com.albertkhang.tunedaily.R;
        import com.albertkhang.tunedaily.activities.InSearchActivity;
        import com.albertkhang.tunedaily.adapters.PlaylistAdapter;
        import com.albertkhang.tunedaily.adapters.TrackAdapter;
        import com.albertkhang.tunedaily.events.UpdateDownloadedTrack;
        import com.albertkhang.tunedaily.events.UpdateFavouriteTrack;
        import com.albertkhang.tunedaily.events.UpdateLanguageEvent;
        import com.albertkhang.tunedaily.models.Playlist;
        import com.albertkhang.tunedaily.utils.FirebaseManager;
        import com.albertkhang.tunedaily.utils.SettingManager;
        import com.albertkhang.tunedaily.models.Track;
        import com.albertkhang.tunedaily.events.UpdateThemeEvent;
        import com.facebook.shimmer.ShimmerFrameLayout;

        import org.greenrobot.eventbus.EventBus;
        import org.greenrobot.eventbus.Subscribe;
        import org.greenrobot.eventbus.ThreadMode;

        import java.io.Serializable;
        import java.util.ArrayList;

public class SearchFragment extends Fragment implements Serializable {
    private FrameLayout search_frame;
    private FrameLayout root_view;
    private SettingManager settingManager;
    private TextView txtSearchTitle;
    private ImageView imgSearch;
    private TextView txtSearchText;
    private TextView txtRandomArtist;
    private TextView txtRandomTracks;
    private ShimmerFrameLayout shimmer_random_artists;
    private ShimmerFrameLayout shimmer_random_songs;
    private RecyclerView rvRandomArtists;
    private RecyclerView rvRandomSongs;
    private PlaylistAdapter randomArtistsAdapter;
    private TrackAdapter randomSongsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        return inflater.inflate(R.layout.fragment_search, container, false);
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateTheme();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTheme();
        randomSongsAdapter.update();
    }

    private void addControl(View view) {
        search_frame = view.findViewById(R.id.search_frame);
        root_view = view.findViewById(R.id.root_view);
        txtSearchTitle = view.findViewById(R.id.txtSearchTitle);
        imgSearch = view.findViewById(R.id.imgSearch);
        txtSearchText = view.findViewById(R.id.txtSearchText);
        txtRandomArtist = view.findViewById(R.id.txtRandomArtist);
        txtRandomTracks = view.findViewById(R.id.txtRandomTracks);
        shimmer_random_artists = view.findViewById(R.id.shimmer_random_artists);
        shimmer_random_songs = view.findViewById(R.id.shimmer_random_songs);
        rvRandomArtists = view.findViewById(R.id.rvRandomArtists);
        rvRandomSongs = view.findViewById(R.id.rvRandomSongs);

        settingManager = SettingManager.getInstance(getContext());

        //Random Artists
        randomArtistsAdapter = new PlaylistAdapter(getContext());
        rvRandomArtists.setAdapter(randomArtistsAdapter);

        LinearLayoutManager managerArtists = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        managerArtists.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvRandomArtists.setLayoutManager(managerArtists);

        //Random Songs
        randomSongsAdapter = new TrackAdapter(getContext());
        rvRandomSongs.setAdapter(randomSongsAdapter);

        LinearLayoutManager managerSongs = new LinearLayoutManager(getContext());
        managerSongs.setOrientation(LinearLayoutManager.VERTICAL);
        rvRandomSongs.setLayoutManager(managerSongs);
        rvRandomSongs.setNestedScrollingEnabled(false);

        updateList();
        updateTheme();
    }

    private void updateList() {
        updateRandomArtists();
        updateRandomSongs();
    }

    private void updateRandomSongs() {
        shimmer_random_songs.setVisibility(View.VISIBLE);
        rvRandomSongs.setVisibility(View.GONE);

        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        firebaseManager.setReadRandomSongsListener(new FirebaseManager.ReadRandomSongsListener() {
            @Override
            public void readRandomSongsListener(final ArrayList<Track> tracks) {
                shimmer_random_songs.setVisibility(View.GONE);
                rvRandomSongs.setVisibility(View.VISIBLE);

                randomSongsAdapter.setOnMoreListener(new TrackAdapter.OnMoreListener() {
                    @Override
                    public void onMoreListener(View view, int position) {
                        showMoreItem(tracks.get(position));
                    }
                });

                randomSongsAdapter.update(tracks);
            }
        });

        firebaseManager.getRandomSongs();
    }

    private void addEvent() {
        search_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), InSearchActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateRandomArtists() {
        shimmer_random_artists.setVisibility(View.VISIBLE);
        rvRandomArtists.setVisibility(View.GONE);

        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        firebaseManager.setReadRandomArtistsListener(new FirebaseManager.ReadRandomArtistsListener() {
            @Override
            public void readRandomArtistsListener(final ArrayList<Playlist> playlists) {
                Log.d("firebaseManager", playlists.toString());

                shimmer_random_artists.setVisibility(View.GONE);
                rvRandomArtists.setVisibility(View.VISIBLE);

                randomArtistsAdapter.update(playlists);
            }
        });

        firebaseManager.getRandomArtist();
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));
            txtSearchTitle.setTextColor(getResources().getColor(R.color.colorLight1));

            setSearchBackground(R.color.colorLight1);
            imgSearch.setColorFilter(getResources().getColor(R.color.colorDark1));
            txtSearchText.setTextColor(getResources().getColor(R.color.colorDark1));

            txtRandomArtist.setTextColor(getResources().getColor(R.color.colorLight1));
            txtRandomTracks.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));
            txtSearchTitle.setTextColor(getResources().getColor(R.color.colorDark1));

            setSearchBackground(R.color.colorDark1);
            imgSearch.setColorFilter(getResources().getColor(R.color.colorLight1));
            txtSearchText.setTextColor(getResources().getColor(R.color.colorLight1));

            txtRandomArtist.setTextColor(getResources().getColor(R.color.colorDark1));
            txtRandomTracks.setTextColor(getResources().getColor(R.color.colorDark1));
        }

        randomArtistsAdapter.notifyDataSetChanged();
        randomSongsAdapter.notifyDataSetChanged();
    }

    private void updateLanguage() {
        settingManager.updateLanguageConfiguration();

        txtSearchTitle.setText(getString(R.string.search));
        txtSearchText.setText(getString(R.string.artist_songs_or_album));
        txtRandomArtist.setText(getString(R.string.random_artists));
        txtRandomTracks.setText(getString(R.string.random_songs));
    }

    private void setSearchBackground(int color) {
        Drawable drawable = getResources().getDrawable(R.drawable.search_background);
        drawable.setTint(getResources().getColor(color));
        search_frame.setBackground(drawable);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateThemeEvent updateThemeEvent) {
        updateTheme();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateLanguageEvent updateLanguageEvent) {
        updateLanguage();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateDownloadedTrack updateDownloadedTrack) {
        randomSongsAdapter.update();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayAction(UpdateFavouriteTrack updateFavouriteTrack) {
        randomSongsAdapter.update();
    }

    private void showMoreItem(Track track) {
        TrackMoreFragment trackMoreFragment = new TrackMoreFragment(track);
        trackMoreFragment.show(requireActivity().getSupportFragmentManager(), "FragmentTrackMore");
    }
}

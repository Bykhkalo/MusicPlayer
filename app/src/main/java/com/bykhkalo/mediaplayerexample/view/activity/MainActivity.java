package com.bykhkalo.mediaplayerexample.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mediaplayerexample.R;
import com.bykhkalo.mediaplayerexample.model.MusicFile;
import com.bykhkalo.mediaplayerexample.service.MusicService;
import com.bykhkalo.mediaplayerexample.service.ServiceBinder;
import com.bykhkalo.mediaplayerexample.utils.DebugUtils;
import com.bykhkalo.mediaplayerexample.view.adapter.ViewPagerAdapter;
import com.bykhkalo.mediaplayerexample.view.fragments.AlbumFragment;
import com.bykhkalo.mediaplayerexample.view.fragments.SongFragment;
import com.bykhkalo.mediaplayerexample.viewmodel.MainViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private List<MusicFile> musicFileList;

    private MainViewModel viewModel;
    private ViewPagerAdapter viewPagerAdapter;


    private TabLayout tabLayout;
    private ViewPager viewPager;


    private MusicService musicService;
    private ServiceConnection serviceConnection;

    private RelativeLayout musicBottomBar;
    private ImageView currentTrackArt;
    private TextView currentTrackName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }



    private void init() {
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        musicBottomBar = findViewById(R.id.music_bottom_bar);
        currentTrackArt = findViewById(R.id.music_img);
        currentTrackName = findViewById(R.id.music_file_name);

        initViewPager();

        initServiceConnection();
        startMusicService();
    }

    private void initViewPager() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), ViewPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPagerAdapter.addFragment(new SongFragment(), "Songs");
        viewPagerAdapter.addFragment(new AlbumFragment(), "Albums");


        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void initBottomBar() {
        if (musicService.getPosition() != -1) {

            musicBottomBar.setVisibility(View.VISIBLE);
            musicBottomBar.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, PlayerActivity.class));
            });

            currentTrackName.setText(musicService.getCurrentFile().getTitle());

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(musicService.getUri().toString());
            byte[] art = retriever.getEmbeddedPicture();

            if (art != null) {

                Glide.with(MainActivity.this)
                        .asBitmap()
                        .load(art)
                        .error(R.drawable.default_album_art)
                        .into(currentTrackArt);
            } else {

                Glide.with(MainActivity.this)
                        .asBitmap()
                        .load(R.drawable.default_album_art)
                        .into(currentTrackArt);
            }

        } else musicBottomBar.setVisibility(View.GONE);
    }




    private void initServiceConnection() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ServiceBinder binder = (ServiceBinder) service;
                musicService = (MusicService) binder.getService();
                Log.d(DebugUtils.TAG, "SongFragment: onServiceConnected: Service connection Success!");

                initBottomBar();

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(DebugUtils.TAG, "SongFragment: onServiceDisconnected: ");
            }
        };
    }


    private void startMusicService() {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        startService(intent);

        bindService(new Intent(MainActivity.this, MusicService.class), serviceConnection, 0);
    }


    @Override
    public void onResume() {
        super.onResume();

        if (musicService != null) {
            initBottomBar();

            musicService.getIsPositionChanged().observe(MainActivity.this, isPositionChanged -> {
                initBottomBar();
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindService(serviceConnection);
    }
}
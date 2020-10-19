package com.bykhkalo.mediaplayerexample.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.palette.graphics.Palette;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mediaplayerexample.R;
import com.bykhkalo.mediaplayerexample.model.MusicFile;
import com.bykhkalo.mediaplayerexample.service.MusicService;
import com.bykhkalo.mediaplayerexample.service.ServiceBinder;
import com.bykhkalo.mediaplayerexample.utils.DebugUtils;
import com.bykhkalo.mediaplayerexample.viewmodel.PlayViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PlayerActivity extends AppCompatActivity implements Runnable {
    private PlayViewModel viewModel;

    private TextView songName, artistName, durationPlayed, durationTotal;
    private ImageView coverArt, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn, gradient;
    private FloatingActionButton playPauseBtn;
    private SeekBar seekBar;
    private RelativeLayout mainContainer;

    private int position = -1;
    private boolean isSeekBarAvailable = true;

    private Handler handler = new Handler();

    private MusicService musicService;
    private ServiceConnection serviceConnection;

    private boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        init();
    }

    private void init() {
        viewModel = ViewModelProviders.of(this).get(PlayViewModel.class);
        position = getIntent().getIntExtra("position", -1);

        bindViewsWithId();
        initServieConnection();
        startMusicService();


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarAvailable = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.seekTo(seekBar.getProgress());
                isSeekBarAvailable = true;
            }
        });

        PlayerActivity.this.runOnUiThread(this);
    }

    private void initServieConnection() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ServiceBinder binder = (ServiceBinder) service;
                musicService = (MusicService) binder.getService();
                Log.d(DebugUtils.TAG, "onServiceConnected: Service connection Success!");

                String action = getIntent().getStringExtra("action");
                if (action != null) {
                    switch (action) {
                        case "play":
                            musicService.startMusicPlayerWork(position);
                            break;
                        case "play_album":
                            musicService.startMusicPlayerWorkWithAlbum(position);
                        default:
                            break;
                    }
                }

                initMetaData(musicService.getCurrentMusicDuration(), musicService.getCurrentFile());
                initButtonsOnClickListeners();

                isBound = true;

                observ();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound = false;
                Log.d(DebugUtils.TAG, "onServiceDisconnected: Service disconnected!");
            }
        };
    }

    private void observ() {
        musicService.getPlayingStatus().observe(this, isPlaying -> {
            if (isPlaying) playPauseBtn.setImageResource(R.drawable.ic_pause);
            else playPauseBtn.setImageResource(R.drawable.ic_play_arrow);
        });

        musicService.getIsPositionChanged().observe(PlayerActivity.this, isPositionChanged -> {
            if (isPositionChanged)
                initMetaData(musicService.getCurrentMusicDuration(), musicService.getCurrentFile());
        });
    }

    private void startMusicService() {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        startService(intent);


        bindService(new Intent(PlayerActivity.this, MusicService.class), serviceConnection, 0);
    }

    @Override
    protected void onStop() {
        if (musicService != null && isBound){
            unbindService(serviceConnection);
            isBound = false;
        }

        super.onStop();
    }

    private void initButtonsOnClickListeners() {
        prevBtn.setOnClickListener(v -> {
            prevBtnClicked();
        });

        nextBtn.setOnClickListener(v -> {
            nextBtnClicked();
        });

        playPauseBtn.setOnClickListener(v -> {
            playPauseBtnClicked();
        });
    }

    private String getFormattedPlayTime(int currentPosition) {

        String totalOut = "";
        String totalNew = "";
        String seconds = String.valueOf(currentPosition % 60);
        String minutes = String.valueOf(currentPosition / 60);

        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;

        if (seconds.length() == 1) return totalNew;
        else return totalOut;
    }


    private void bindViewsWithId() {

        //TextViews
        songName = findViewById(R.id.song_name);
        artistName = findViewById(R.id.song_arist);
        durationPlayed = findViewById(R.id.track_played_duration);
        durationTotal = findViewById(R.id.track_total_duration);

        //ImageViews
        coverArt = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.next);
        prevBtn = findViewById(R.id.previous);
        backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.shuffle);
        repeatBtn = findViewById(R.id.repeat);
        gradient = findViewById(R.id.image_view_gradient);

        //FABs
        playPauseBtn = findViewById(R.id.play_pause_btn);


        //SeekBars
        seekBar = findViewById(R.id.seekBar);

        //Leyouts
        mainContainer = findViewById(R.id.container);
    }

    private void initMetaData(int currentMusicDuration, MusicFile musicFile) {
        songName.setText(musicFile.getTitle());
        artistName.setText(musicFile.getArtist());
        seekBar.setMax(currentMusicDuration / 1000);
        durationTotal.setText(getFormattedPlayTime(currentMusicDuration / 1000));

        if (musicService.isMusicPlaying()) playPauseBtn.setImageResource(R.drawable.ic_pause);
        else playPauseBtn.setImageResource(R.drawable.ic_play_arrow);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(musicFile.getPath());
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;

        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            Palette.from(bitmap).generate(palette -> {
                Palette.Swatch swatch = palette.getDominantSwatch();

                if (swatch != null) {
                    gradient.setBackgroundResource(R.drawable.gradient_bg);
                    mainContainer.setBackgroundResource(R.drawable.main_bg);

                    GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{swatch.getRgb(), 0x00000000});
                    gradient.setBackground(gradientDrawable);

                    GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{swatch.getRgb(), swatch.getRgb()});
                    mainContainer.setBackground(gradientDrawableBg);

                    songName.setTextColor(swatch.getTitleTextColor());
                    artistName.setTextColor(swatch.getBodyTextColor());

                } else {
                    GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff000000, 0x00000000});
                    // gradient.setBackground(ContextCompat.getDrawable(PlayerActivity.this, R.drawable.main_bg));
                    gradient.setBackground(gradientDrawable);

                    GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff000000, 0xff000000});
                    //mainContainer.setBackground(ContextCompat.getDrawable(PlayerActivity.this, R.drawable.gradient_bg));
                    mainContainer.setBackground(gradientDrawableBg);

                    songName.setTextColor(ContextCompat.getColor(PlayerActivity.this, R.color.colorAccent));
                    artistName.setTextColor(ContextCompat.getColor(PlayerActivity.this, R.color.colorAccent));
                }

            });

            Glide.with(this)
                    .asBitmap()
                    .load(art)
                    .into(coverArt);
        } else {

            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff000000, 0x00000000});
            // gradient.setBackground(ContextCompat.getDrawable(PlayerActivity.this, R.drawable.main_bg));
            gradient.setBackground(gradientDrawable);

            GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff000000, 0xff000000});
            //mainContainer.setBackground(ContextCompat.getDrawable(PlayerActivity.this, R.drawable.gradient_bg));
            mainContainer.setBackground(gradientDrawableBg);

            songName.setTextColor(ContextCompat.getColor(PlayerActivity.this, R.color.colorAccent));
            artistName.setTextColor(ContextCompat.getColor(PlayerActivity.this, R.color.colorAccent));

            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.default_album_art)
                    .into(coverArt);
        }

    }


    private void nextBtnClicked() {
        boolean isPlaying = musicService.isMusicPlaying();


        playNextTrack();

        if (isPlaying) {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            musicService.resumePlaying();

        } else playPauseBtn.setImageResource(R.drawable.ic_play_arrow);
    }

    private void prevBtnClicked() {
        boolean isPlaying = musicService.isMusicPlaying();


        playPrevTrack();

        if (isPlaying) {
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            musicService.resumePlaying();

        } else playPauseBtn.setImageResource(R.drawable.ic_play_arrow);

    }

    private void playPrevTrack() {
        musicService.playPrev();
        initMetaData(musicService.getCurrentMusicDuration(), musicService.getCurrentFile());
    }

    private void playNextTrack() {
        musicService.playNext();
        initMetaData(musicService.getCurrentMusicDuration(), musicService.getCurrentFile());
    }

    private void playPauseBtnClicked() {
        if (musicService.isMusicPlaying())
            musicService.pausePlaying();
        else
            musicService.resumePlaying();

    }


    @Override
    public void run() {
        if (musicService != null) {
            int mCurrentPosition = musicService.getCurrentPositionInSong();

            if (isSeekBarAvailable)
                seekBar.setProgress(mCurrentPosition);
            durationPlayed.setText(getFormattedPlayTime(mCurrentPosition));
        }

        handler.postDelayed(this, 500);
    }

    public void backOnClick(View view) {
        onBackPressed();
    }

    public void goToMusicList(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
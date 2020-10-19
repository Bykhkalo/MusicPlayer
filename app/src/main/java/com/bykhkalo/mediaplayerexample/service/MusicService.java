package com.bykhkalo.mediaplayerexample.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.MutableLiveData;

import com.example.mediaplayerexample.R;
import com.bykhkalo.mediaplayerexample.model.Album;
import com.bykhkalo.mediaplayerexample.model.MusicFile;
import com.bykhkalo.mediaplayerexample.model.MusicFileStorage;
import com.bykhkalo.mediaplayerexample.utils.DebugUtils;
import com.bykhkalo.mediaplayerexample.view.activity.PlayerActivity;

import java.io.File;
import java.util.List;

import static androidx.core.app.NotificationCompat.*;

public class MusicService extends Service {

    public static final String ACTION_START_STOP = "action_start_stop";
    public static final String ACTION_PLAY_PREVIOUS = "action_play_previous";
    public static final String ACTION_PLAY_NEXT = "action_play_next";

    private final IBinder binder = new ServiceBinder(this);


    private MutableLiveData<Boolean> playingStatus;
    private MusicFileStorage musicFileStorage;
    private List<MusicFile> musicList;
    private List<Album> albumList;
    private Uri uri;
    private MediaPlayer mediaPlayer;
    private int position = -1;
    private int albumPositionPointer;

    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;

    private Album currentAlbum = null;

    private MutableLiveData<Boolean> isPositionChanged;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initBroadcastReceiver();
        registerReceiver(receiver, intentFilter);
        Log.d(DebugUtils.TAG, "MusicService: initBroadcastReceiver: receiver registered!");

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isPositionChanged = new MutableLiveData<>();
        playingStatus = new MutableLiveData<>();
        musicFileStorage = MusicFileStorage.getInstance();
        musicList = musicFileStorage.getMusicList();
        albumList = musicFileStorage.getAlbumList();


        return START_REDELIVER_INTENT;
    }

    private void initBroadcastReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(DebugUtils.TAG, "onReceive: ");
                String action = intent.getAction();
                boolean isPlaying = mediaPlayer.isPlaying();
                if (action != null)
                    switch (action) {
                        case ACTION_START_STOP:
                            startOrStop();
                            break;

                        case ACTION_PLAY_PREVIOUS:
                            playPrev();
                            if (isPlaying) mediaPlayer.start();
                            isPositionChanged.setValue(true);
                            break;
                        case ACTION_PLAY_NEXT:
                            playNext();
                            if (isPlaying) mediaPlayer.start();
                            isPositionChanged.setValue(true);
                            break;
                        default:
                            break;
                    }
            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_START_STOP);
        intentFilter.addAction(ACTION_PLAY_PREVIOUS);
        intentFilter.addAction(ACTION_PLAY_NEXT);
    }

    private void initMediaPlayer() {
        Log.d(DebugUtils.TAG, "initMediaPlayer: position: " + position);

        if (!musicList.isEmpty()) {
            Log.d(DebugUtils.TAG, "handleIntent: array size = " + musicList.size());

            playingStatus.setValue(true);

            refreshUri();
        }

        if (mediaPlayer != null) {

            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = createMediaPlayer();


        if (mediaPlayer != null)
            mediaPlayer.start();


        makeNotification(mediaPlayer.isPlaying());
    }

    public void startMusicPlayerWork(int musicPosition) {
        currentAlbum = null;


        position = musicPosition;
        initMediaPlayer();
    }

    public void startMusicPlayerWorkWithAlbum(int albumPosition){

        Album newAlbum = albumList.get(albumPosition);

        if (newAlbum.equals(currentAlbum)) return;
        else
            currentAlbum = newAlbum;

        playAlbum();
        initMediaPlayer();
    }



    private MediaPlayer createMediaPlayer() {

        MediaPlayer player = MediaPlayer.create(getApplicationContext(), uri);

        if (player != null)
            player.setOnCompletionListener(mp -> {
                playNext();
                mediaPlayer.start();
                isPositionChanged.setValue(true);
                makeNotification(mediaPlayer.isPlaying());

            });


        return player;
    }

    public void startOrStop() {
        if (mediaPlayer.isPlaying()) pausePlaying();
        else resumePlaying();
    }

    public void pausePlaying() {
        mediaPlayer.pause();
        playingStatus.setValue(false);

        makeNotification(mediaPlayer.isPlaying());
    }

    public void resumePlaying() {
        mediaPlayer.start();
        playingStatus.setValue(true);
        makeNotification(mediaPlayer.isPlaying());

    }

    public boolean isMusicPlaying() {
        return mediaPlayer.isPlaying();
    }

    public Uri getUri() {
        return Uri.parse(musicList.get(position).getPath());
    }

    public int getCurrentMusicDuration() {
        return mediaPlayer.getDuration();
    }

    public void seekTo(int value) {
        if (mediaPlayer != null)
            mediaPlayer.seekTo(value * 1000);
    }

    public void playPrev() {
        boolean isPlaying = mediaPlayer.isPlaying();

        mediaPlayer.stop();
        mediaPlayer.release();

        if (currentAlbum == null)
            position = (position - 1) < 0 ? ((musicList).size() - 1) : (position - 1);
        else
        {
            if (albumPositionPointer -1 < 0) {
                position = currentAlbum.getFilePositions().get(currentAlbum.getFilePositions().size() -1);
                albumPositionPointer = currentAlbum.getFilePositions().size() - 1;
            }else position = currentAlbum.getFilePositions().get(--albumPositionPointer);
        }
            //position = (currentAlbum.getFilePositions().get(--albumPositionPointer)) < 0 ? ((currentAlbum.getFilePositions()).size() - 1) : (currentAlbum.getFilePositions().get(--albumPositionPointer));

        if (currentAlbum != null){
            for (int i : currentAlbum.getFilePositions()){
                Log.d(DebugUtils.TAG, "playPrev: Founded : " + i);
            }
        }

        Log.d(DebugUtils.TAG, "playPrev: position selected: " + position);

        refreshUri();

        mediaPlayer = createMediaPlayer();
        makeNotification(isPlaying);
    }

    public void playNext() {
        boolean isPlaying = mediaPlayer.isPlaying();

        mediaPlayer.stop();
        mediaPlayer.release();

        if (currentAlbum == null)
            position = (++position % musicList.size());
        else
        {
            if (albumPositionPointer + 1 >= currentAlbum.getFilePositions().size()){
                albumPositionPointer = 0;
                position = currentAlbum.getFilePositions().get(albumPositionPointer);
            }else {
                position = currentAlbum.getFilePositions().get(++albumPositionPointer);
            }
        }
            //position = currentAlbum.getFilePositions().get(++albumPositionPointer % currentAlbum.getFilePositions().size());


        refreshUri();

        mediaPlayer = createMediaPlayer();
        makeNotification(isPlaying);

    }

    public MutableLiveData<Boolean> getIsPositionChanged() {
        return isPositionChanged;
    }

    public MutableLiveData<Boolean> getPlayingStatus() {
        return playingStatus;
    }

    public int getCurrentPositionInSong() {
        int curPos = 0;
        if (mediaPlayer != null) {
            curPos = mediaPlayer.getCurrentPosition();
        }
        return curPos / 1000;
    }

    public MusicFile getCurrentFile() {
        return musicList.get(position);
    }

    private void makeNotification(boolean isPlaying) {
        MediaSession mediaSession = new MediaSession(this, "tag");

        int startStopIconId;
        if (isPlaying) startStopIconId = R.drawable.ic_pause;
        else startStopIconId = R.drawable.ic_play_arrow;

        String trackName = getCurrentFile().getTitle();
        String trackAlbum = getCurrentFile().getAlbum();

        byte[] image = getAlbumArt(getCurrentFile().getPath());
        Bitmap albumArt;

        if (image != null)
            albumArt = BitmapFactory.decodeByteArray(image, 0, image.length);
        else
            albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art);


        String chanel_id = "3000";
        CharSequence name = "MusicChannel";
        String description = "For showing data about current music track";


        Intent intent = new Intent(MusicService.this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notification_manager = (NotificationManager) MusicService.this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(chanel_id, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            notification_manager.createNotificationChannel(mChannel);

        }

        NotificationCompat.Builder notification_builder = new NotificationCompat.Builder(this, chanel_id);

        if (startStopIconId == R.drawable.ic_play_arrow) notification_builder.setOngoing(false);
        else notification_builder.setOngoing(true);

        Notification notification = notification_builder
                .setSmallIcon(startStopIconId)
                .setContentTitle(trackName)
                .setContentText(trackAlbum)
                .setLargeIcon(albumArt)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setPriority(PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .addAction(R.drawable.ic_skip_previous, "Previous", makePendingIntentForBroadcast(ACTION_PLAY_PREVIOUS))
                .addAction(startStopIconId, "Pause", makePendingIntentForBroadcast(ACTION_START_STOP))
                .addAction(R.drawable.ic_skip_next, "Next", makePendingIntentForBroadcast(ACTION_PLAY_NEXT))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2))
                .build();


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MusicService.this);
        notificationManagerCompat.notify(1, notification);


    }

    private PendingIntent makePendingIntentForBroadcast(String action) {
        Intent intent = new Intent(action);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private byte[] getAlbumArt(String uri) {
        // Log.d(DebugUtils.TAG, "MusicService: getAlbumArt: image path: " + uri);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();

        return art;
    }

    private void playAlbum() {

        for (int i : currentAlbum.getFilePositions()){
            Log.d(DebugUtils.TAG, "playAlbum: songs: " + musicList.get(i).toString());
        }

        albumPositionPointer = 0;

        position = currentAlbum.getFilePositions().get(albumPositionPointer);

    }

    private void refreshUri() {
        uri = Uri.fromFile(new File(musicList.get(position).getPath()));

        // Log.d(DebugUtils.TAG, "refreshUri: from file: " + uri.toString());

        //  Log.d(DebugUtils.TAG, "refreshUri: parseUri: " + Uri.parse(musicList.get(position).getPath()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
        Log.d(DebugUtils.TAG, "MusicService: onDestroy: receiver unregistered!");
    }

    public int getPosition() {
        return position;
    }
}


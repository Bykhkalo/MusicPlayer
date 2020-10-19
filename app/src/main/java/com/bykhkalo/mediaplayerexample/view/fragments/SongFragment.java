package com.bykhkalo.mediaplayerexample.view.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mediaplayerexample.R;
import com.bykhkalo.mediaplayerexample.model.Album;
import com.bykhkalo.mediaplayerexample.model.MusicFile;
import com.bykhkalo.mediaplayerexample.view.adapter.MusicAdapter;
import com.bykhkalo.mediaplayerexample.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SongFragment extends Fragment {

    private final int PERMISSION_REQUEST_CODE = 1;


    private MainViewModel viewModel;

    private RecyclerView musicRecyclerView;
    private MusicAdapter adapter;

    private ProgressBar progressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_song, container, false);

        init(view);
        observ();

        return view;
    }

    private void observ() {
        viewModel.getIsStorageUpdated().observe(SongFragment.this, isStorageUpdated -> {
            progressBar.setVisibility(View.GONE);
            if (isStorageUpdated) adapter.notifyDataSetChanged();
        });

    }


    private void init(View view) {
        viewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        musicRecyclerView = view.findViewById(R.id.music_recyclerView);
        progressBar = view.findViewById(R.id.scanProgress);

        initRecyclerView();
        getPermissions();


    }


    private void getPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permissions NOT GRANTED!", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            if (viewModel.getExitingMusic().isEmpty())
                scanMusic(getContext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Toast.makeText(getContext(), "onRequestPermissionResult", Toast.LENGTH_SHORT).show();

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (viewModel.getExitingMusic().isEmpty())
                    scanMusic(getContext());
            } else {
                Toast.makeText(getContext(), "Permissions NOT GRANTED!", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    public void scanMusic(Context context) {


        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {


            List<MusicFile> tempAudioList = new ArrayList<>();
           Map<String, Album> tempAlbumMap = new HashMap<>();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.TITLE,
                    "duration",
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.ALBUM_ID

            };

            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null) {

                while (cursor.moveToNext()) {
                    String album = cursor.getString(0);
                    String title = cursor.getString(1);
                    String duration = cursor.getString(2);
                    String path = cursor.getString(3);
                    String artist = cursor.getString(4);
                    int size = cursor.getInt(5);
                    String albumId = cursor.getString(6);

                    if (size == 0) continue;
                    if (duration == null || duration.equals(String.valueOf(0))) continue;

                    MusicFile musicFile = new MusicFile(path, title, artist, album, duration, size, albumId);

                    tempAudioList.add(musicFile);
                    tempAlbumMap.put(album, new Album(album, albumId));

                    //  Log.d(DebugUtils.TAG, "scanMusic: item: " + musicFile.toString());
                }
                cursor.close();

            }

            viewModel.rewriteMusicStorage(tempAudioList, new ArrayList<>(tempAlbumMap.values()));


        }).start();


    }

    private void initRecyclerView() {
        adapter = new MusicAdapter(getContext(), viewModel.getExitingMusic());
        adapter.setHasStableIds(true);


        musicRecyclerView.setHasFixedSize(true);
        musicRecyclerView.setAdapter(adapter);
        musicRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        musicRecyclerView.setItemViewCacheSize(20);
        musicRecyclerView.setDrawingCacheEnabled(true);
        musicRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

    }


}
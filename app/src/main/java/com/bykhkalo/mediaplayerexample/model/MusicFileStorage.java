package com.bykhkalo.mediaplayerexample.model;

import java.util.ArrayList;
import java.util.List;

public class MusicFileStorage {

    private static MusicFileStorage instance;

    private MusicFileStorage() {
        musicList = new ArrayList<>();
        albumList = new ArrayList<>();
    }

    public static MusicFileStorage getInstance(){
        if (instance == null) instance = new MusicFileStorage();
        return instance;
    }


    private List<MusicFile> musicList;
    private List<Album> albumList;


    public List<Album> getAlbumList() {
        return albumList;
    }

    public List<MusicFile> getMusicList() {
        return musicList;
    }


}

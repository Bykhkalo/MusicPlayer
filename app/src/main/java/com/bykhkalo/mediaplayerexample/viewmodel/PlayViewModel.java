package com.bykhkalo.mediaplayerexample.viewmodel;

import androidx.lifecycle.ViewModel;

import com.bykhkalo.mediaplayerexample.model.MusicFile;
import com.bykhkalo.mediaplayerexample.model.MusicFileStorage;

import java.util.List;

public class PlayViewModel extends ViewModel {

    private MusicFileStorage musicFileStorage;

    public PlayViewModel() {
        musicFileStorage = MusicFileStorage.getInstance();
    }

    public List<MusicFile> getExitingMusic(){
        return musicFileStorage.getMusicList();
    }


}

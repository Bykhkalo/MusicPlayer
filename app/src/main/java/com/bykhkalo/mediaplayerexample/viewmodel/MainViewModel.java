package com.bykhkalo.mediaplayerexample.viewmodel;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bykhkalo.mediaplayerexample.model.Album;
import com.bykhkalo.mediaplayerexample.model.MusicFile;
import com.bykhkalo.mediaplayerexample.model.MusicFileStorage;

import java.util.List;

public class MainViewModel extends ViewModel {

    private MutableLiveData<Boolean> isStorageUpdated;
    private MutableLiveData<Boolean> isAlbumListUpdated;

    private MusicFileStorage musicFileStorage;

    public MainViewModel() {
        isStorageUpdated = new MutableLiveData<>();
        isAlbumListUpdated = new MutableLiveData<>();

        musicFileStorage = MusicFileStorage.getInstance();
    }


    public List<MusicFile> getExitingMusic() {
        return musicFileStorage.getMusicList();
    }

    public List<Album> getAlbums() {
        return musicFileStorage.getAlbumList();
    }

    public void rewriteMusicStorage(List<MusicFile> musicFileList) {

        musicFileStorage.getMusicList().clear();
        musicFileStorage.getMusicList().addAll(musicFileList);
        isStorageUpdated.postValue(true);
    }

    public void rewriteMusicStorage(List<MusicFile> musicFileList, List<Album> albums) {

        musicFileStorage.getAlbumList().clear();


        for (MusicFile file : musicFileList){
            for (Album album : albums){
                if (album.getAlbumName().equals( file.getAlbum())) {
                    album.getFilePositions().add(musicFileList.indexOf(file));
                    album.setArt(file.getPath());

                    //Log.d(DebugUtils.TAG, "rewriteMusicStorage: File Found its album: " + album.getAlbumName() + "file: " + file.getTitle());
                    break;
                }
            }
        }

        rewriteMusicStorage(musicFileList);
        musicFileStorage.getAlbumList().addAll(albums);
        isAlbumListUpdated.postValue(true);

    }


    public MutableLiveData<Boolean> getIsStorageUpdated() {
        return isStorageUpdated;
    }

    public MutableLiveData<Boolean> getIsAlbumListUpdated() {
        return isAlbumListUpdated;
    }
}

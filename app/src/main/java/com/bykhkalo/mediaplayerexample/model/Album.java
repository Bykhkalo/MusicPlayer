package com.bykhkalo.mediaplayerexample.model;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.bykhkalo.mediaplayerexample.utils.DebugUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Album {

    private static ExecutorService service = Executors.newFixedThreadPool(5);


    private String albumName;
    private String albumId;

    private byte[] artImage = new byte[0];
    private int imageLoadingStatus = -1;


    private List<Integer> filePositions;

    public Album(String albumName, String albumId) {
        this.albumName = albumName;
        this.albumId = albumId;


        filePositions = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Album{" +
                "albumName='" + albumName + '\'' +
                ", albumId='" + albumId + '\'' +
                '}' + '\n';
    }

    public synchronized void setArt(String uri){
        if (artImage == null) return;

        if (artImage.length == 0)
        service.submit(() -> {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            try {
                retriever.setDataSource(uri);
                artImage = retriever.getEmbeddedPicture();
                retriever.release();


            }catch (Exception e){
                Log.d(DebugUtils.TAG, "Error: " + e.toString());
            }


            if (artImage.length == 0){
                imageLoadingStatus = 0;
            } else imageLoadingStatus = 1;

        });


    }

}

package com.bykhkalo.mediaplayerexample.model;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.bykhkalo.mediaplayerexample.utils.DebugUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MusicFile {

    private static ExecutorService service = Executors.newFixedThreadPool(5);

    private String path;
    private String title;
    private String artist;
    private String album;
    private String duration;
    private int size;
    private String albumId;


    private byte[] alumArtImage;
    private int imageLoadingStatus = -1;


    public MusicFile(String path, String title, String artist, String album, String duration, int size, String albumId) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.size = size;
        this.albumId = albumId;


       service.submit(() -> {
          alumArtImage = getAlbumArt(path);

          if (alumArtImage.length == 0){
              imageLoadingStatus = 0;
          } else imageLoadingStatus = 1;

       });

    }

    private byte[] getAlbumArt(String  uri){
       // Log.d(DebugUtils.TAG, "getAlbumArt: image path: " + uri);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        byte[] art = new byte[0];



        try {
            retriever.setDataSource(uri);
            art = retriever.getEmbeddedPicture();
            retriever.release();


        }
        catch (Exception e){
            Log.d(DebugUtils.TAG, "Error: " + e.toString());

        }



        return art;
    }

//    @Override
//    public String toString() {
//        return "MusicFile{" +
//                "title='" + title + '\'' +
//                ", duration='" + duration + '\'' +
//                '}';
//    }


    @Override
    public String toString() {
        return "MusicFile{" +
                "path='" + path + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", duration='" + duration + '\'' +
                ", size=" + size +
                ", albumId='" + albumId + '\'' +
                '}' + '\n';
    }
}

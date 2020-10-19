package com.bykhkalo.mediaplayerexample.view.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mediaplayerexample.R;
import com.bykhkalo.mediaplayerexample.model.Album;
import com.bykhkalo.mediaplayerexample.view.activity.PlayerActivity;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private List<Album> albums;

    public AlbumAdapter(List<Album> albums) {
        this.albums = albums;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        holder.bind(albums.get(position));
    }

    @Override
    public long getItemId(int position) {
        return albums.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }


    public static class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView albumArt;
        private TextView albumName;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);

            albumArt = itemView.findViewById(R.id.album_art);
            albumName = itemView.findViewById(R.id.album_name);

            itemView.setOnClickListener(this);
        }

        public void bind(Album album){
            albumName.setText(album.getAlbumName());

            if (album.getImageLoadingStatus() == 1) {
                byte[] image = album.getArtImage();

                Glide.with(itemView.getContext())
                        .asBitmap()
                        .load(image)
                        .override(128, 128)
                        .error(R.drawable.default_album_art)
                        .into(albumArt);
            } else {
                Glide.with(itemView.getContext())
                        .load(R.drawable.default_album_art)
                        .override(128, 128)
                        .into(albumArt);
            }
        }

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(v.getContext(), PlayerActivity.class);
            intent.putExtra("position", getAdapterPosition());
            intent.putExtra("action", "play_album");
            v.getContext().startActivity(intent);
        }
    }


}

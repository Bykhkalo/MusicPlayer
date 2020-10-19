package com.bykhkalo.mediaplayerexample.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mediaplayerexample.R;
import com.bykhkalo.mediaplayerexample.model.MusicFile;
import com.bykhkalo.mediaplayerexample.utils.DebugUtils;
import com.bykhkalo.mediaplayerexample.view.activity.PlayerActivity;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    //private Context context;
    private List<MusicFile> musicFiles;

    public MusicAdapter(Context context, List<MusicFile> musicFiles) {
        // this.context = context;
        this.musicFiles = musicFiles;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        holder.bind(musicFiles.get(position));
    }


    @Override
    public long getItemId(int position) {
        return musicFiles.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return musicFiles.size();
    }

    class MusicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RelativeLayout musicItemLayout;
        private ImageView album_art;
        private TextView fileName;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);


            musicItemLayout = itemView.findViewById(R.id.music_item);
            album_art = itemView.findViewById(R.id.music_img);
            fileName = itemView.findViewById(R.id.music_file_name);

            itemView.setOnClickListener(this);
        }

        public void bind(MusicFile musicFile) {

            fileName.setText(musicFile.getTitle());

            if (musicFile.getImageLoadingStatus() == 1) {
                byte[] image = musicFile.getAlumArtImage();

                Glide.with(itemView.getContext())
                        .asBitmap()
                        .load(image)
                        .override(128, 128)
                        .error(R.drawable.default_album_art)
                        .into(album_art);
            } else {
                Glide.with(itemView.getContext())
                        .load(R.drawable.default_album_art)
                        .override(128, 128)
                        .into(album_art);
            }








        }



        @Override
        public void onClick(View v) {

            Log.d(DebugUtils.TAG, "onClick: " + musicFiles.get(getAdapterPosition()).toString());

            Intent intent = new Intent(v.getContext(), PlayerActivity.class);
            intent.putExtra("position", getAdapterPosition());
            intent.putExtra("action", "play");
            v.getContext().startActivity(intent);
        }
    }


}

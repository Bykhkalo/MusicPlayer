package com.bykhkalo.mediaplayerexample.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mediaplayerexample.R;
import com.bykhkalo.mediaplayerexample.view.adapter.AlbumAdapter;
import com.bykhkalo.mediaplayerexample.viewmodel.MainViewModel;


public class AlbumFragment extends Fragment {

    MainViewModel viewModel;

    RecyclerView albumList;

    AlbumAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_album, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        observ();
    }

    private void observ() {
        viewModel.getIsAlbumListUpdated().observe(AlbumFragment.this, isUpdated -> {
            if (isUpdated) adapter.notifyDataSetChanged();
        });
    }

    private void init(View view) {
        viewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);

        adapter = new AlbumAdapter(viewModel.getAlbums());
        adapter.setHasStableIds(true);

        albumList = view.findViewById(R.id.album_list);
        albumList.setAdapter(adapter);
        albumList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));


    }
}
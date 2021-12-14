package com.example.asus.androidcointracker.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.example.asus.androidcointracker.R;

public class LoadingViewHolder extends RecyclerView.ViewHolder{

    ProgressBar progressBar;

    public LoadingViewHolder(View itemView) {
        super(itemView);

        progressBar = (ProgressBar)itemView.findViewById(R.id.progress_bar);
    }
}

package com.muhammadfarazrashid.i2106595;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.muhammadfarazrashid.i2106595.dataclasses.FirebaseManager;

import java.util.List;
import java.util.Objects;

public class RecentSearchesAdapter extends RecyclerView.Adapter<RecentSearchesAdapter.ViewHolder> {

    private final List<String> recentSearchesList; // Change the type as per your data structure
    private final OnRemoveClickListener onRemoveClickListener;

    String viewType;

    public RecentSearchesAdapter(List<String> recentSearchesList, OnRemoveClickListener onRemoveClickListener, String viewType) {
        this.recentSearchesList = recentSearchesList;
        this.onRemoveClickListener = onRemoveClickListener;
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(Objects.equals(this.viewType, "recentSearches"))
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recentsearchcard, parent, false);
            return new ViewHolder(view);
        }
        else if(this.viewType.equals("notifications"))
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notificationscard, parent, false);
            return new ViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recentsearchcard, parent, false);
            return new ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String recentSearch = recentSearchesList.get(position);
        holder.recentSearchText.setText(recentSearch);

        // Set up the click listener for the remove button
        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRemoveClickListener.onRemoveClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recentSearchesList.size();
    }

    public void removeRecentSearch(int position) {
        if (position >= 0 && position < recentSearchesList.size()) {

            recentSearchesList.remove(position);
            notifyItemRemoved(position);

            if (recentSearchesList.isEmpty()) {
                // Handle the case when the list becomes empty, e.g., show a message
            }
        }
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recentSearchText;
        ImageButton removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recentSearchText = itemView.findViewById(R.id.recentSearchText);
            removeButton = itemView.findViewById(R.id.deleteMentorButton);
        }
    }
}

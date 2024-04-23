package com.muhammadfarazrashid.i2106595;

import com.muhammadfarazrashid.i2106595.dataclasses.FirebaseManager;
import com.muhammadfarazrashid.i2106595.dataclasses.NotificationsManager;
import com.muhammadfarazrashid.i2106595.UserManager;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muhammadfarazrashid.i2106595.dataclasses.NotificationsManager;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class MentorCardAdapter extends RecyclerView.Adapter<MentorCardAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final List<Mentor> data;
    private final int layoutResourceId; // Resource ID of the layout
    private OnItemClickListener listener; // Listener variable

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Add a Firebase Authentication instance variable
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    public MentorCardAdapter(Context context, List<Mentor> data, int layoutResourceId) {
        this.inflater = LayoutInflater.from(context);
        this.data = data;
        this.layoutResourceId = layoutResourceId;
        fetchUserFavorites();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(layoutResourceId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mentor mentor = data.get(position);
        holder.mentorName.setText(mentor.getName());
        //if position has "at" replace it with @
        if(mentor.getPosition().contains("at")){
            holder.mentorPosition.setText(mentor.getPosition().replace("at","@"));
        }
        else
            holder.mentorPosition.setText(mentor.getPosition());

        if (mentor.getAvailability().equals("Available")) {
            // Green color
            holder.mentorAvailability.setTextColor(0xFF3EAD00);
        } else {
            // Gray color
            holder.mentorAvailability.setTextColor(0xFF918C8C);
        }

        holder.mentorAvailability.setText(mentor.getAvailability());

        holder.mentorSalary.setText("$"+mentor.getSalary()+"/hr");

        String mentorId = data.get(position).getId();

        if(!mentor.getprofilePictureUrl().isEmpty()){
            Picasso.get().load(mentor.getprofilePictureUrl()).into(holder.mentorImage);
        }

        holder.heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toggle heart button state
                holder.heartButton.setSelected(!holder.heartButton.isSelected());
                // Change heart button color based on selection
                if (holder.heartButton.isSelected()) {
                    // Set red color for filled heart
                    holder.heartButton.setImageResource(R.drawable.filled_heart);
                    // Add mentor as favorite
                    addFavoriteMentor(mentor.getId());// Call sendNotification to display the notification
                    NotificationsManager.showNotification(view.getContext(), "Mentor " +mentor.getName() + " has been added to your favorites");
                    FirebaseManager firebaseManager = new FirebaseManager();

                    firebaseManager.addNotificationToUser(UserManager.getInstance().getCurrentUser().getId(), "Mentor " +mentor.getName() + " has been added to your favorites", "Favourites");
                } else {
                    holder.heartButton.setImageResource(R.drawable.heart);
                    // Remove mentor from favorites
                    NotificationsManager.showNotification(view.getContext(), "Mentor " +mentor.getName() + " has been removed from your favorites");
                    FirebaseManager firebaseManager = new FirebaseManager();
                    firebaseManager.addNotificationToUser(UserManager.getInstance().getCurrentUser().getId(), "Mentor " +mentor.getName() + " has been added to your favorites", "Favourites");
                    removeFavoriteMentor(mentor.getId());
                }
            }
        });

        if (mentor.isFavorite()) {
            // Set red color for filled heart
            holder.heartButton.setImageResource(R.drawable.filled_heart);
            holder.heartButton.setSelected(true);
        } else {
            holder.heartButton.setImageResource(R.drawable.heart);
            holder.heartButton.setSelected(false);
        }


    }

    private void fetchUserFavorites() {


        for (Mentor mentor : data) {
            mentor.setFavorite(Math.random() < 0.5);
        }


//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("favorites");
//        favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                // Iterate through the favorites and update mentors accordingly
//                for (DataSnapshot mentorSnapshot : snapshot.getChildren()) {
//                    String mentorId = mentorSnapshot.getKey();
//                    if (mentorId != null) { // Add null check
//                        for (Mentor mentor : data) {
//                            if (mentorId.equals(mentor.getId())) { // Check for null mentor ID
//                                mentor.setFavorite(true);
//                                break;
//                            }
//                        }
//                    }
//                }
//                notifyDataSetChanged(); // Update the RecyclerView
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to fetch user favorites: " + error.getMessage());
//            }
//        });
    }

   public void updateList(List<Mentor>mentors)
   {
         data.clear();
         data.addAll(mentors);
         notifyDataSetChanged();
   }


    private void addFavoriteMentor(String mentorId) {
        // Get current user ID
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        // Add mentor to favorites
       DatabaseReference  favoritesRef = database.getReference("users").child(userId).child("favorites").child(mentorId);
        favoritesRef.setValue(true)
                .addOnSuccessListener((OnSuccessListener<Void>) aVoid -> Log.d(TAG, "Favorite mentor added: " + mentorId))
                .addOnFailureListener((OnFailureListener) e -> Log.e(TAG, "Failed to add favorite mentor: " + e.getMessage()));
        //get mentor name using chat adapater

    }

    // Remove favorite mentor for the current user
    private void removeFavoriteMentor(String mentorId) {
        // Get current user ID
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        // Remove mentor from favorites
        DatabaseReference favoritesRef = database.getReference("users").child(userId).child("favorites").child(mentorId);
        favoritesRef.removeValue()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Favorite mentor removed: " + mentorId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to remove favorite mentor: " + e.getMessage()));
    }



    @Override
    public int getItemCount() {
        return data.size();
    }

    // Set item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Interface for item click events
    public interface OnItemClickListener {
        void onItemClick(Mentor position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView mentorName, mentorPosition, mentorAvailability, mentorSalary;
        ImageView mentorImage, heartButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mentorName = itemView.findViewById(R.id.cardName);
            mentorPosition = itemView.findViewById(R.id.cardPosition);
            mentorAvailability = itemView.findViewById(R.id.cardAvailability);
            mentorSalary = itemView.findViewById(R.id.cardSalary);
            mentorImage = itemView.findViewById(R.id.card);
            heartButton = itemView.findViewById(R.id.favouriteButton);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(data.get(position));
                        }
                    }
                }
            });
        }
    }

}

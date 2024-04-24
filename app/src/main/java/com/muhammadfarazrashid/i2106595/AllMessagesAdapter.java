package com.muhammadfarazrashid.i2106595;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllMessagesAdapter extends RecyclerView.Adapter<AllMessagesAdapter.ViewHolder> {

    private List<AllMessagesChat> chatMessages;
    private Context context;

    public AllMessagesAdapter(List<AllMessagesChat> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.chatcard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AllMessagesChat message = chatMessages.get(position);

        int unreadMessageCount=0;
         message.getUnreadMessages(new AllMessagesChat.UnreadMessagesCallback() {
            @Override
            public void onUnreadMessagesCount(int unreadCount) {
                // Update the unread message count
                message.setUnreadMessages(unreadCount);
                holder.unreadMessages.setText(unreadCount + " new message");

                Log.d("AllMessagesChat", "Unread message count: " + unreadCount);

                // Set text color based on the number of unread messages
                if (unreadCount == 0) {
                    holder.unreadMessages.setTextColor(ContextCompat.getColor(context, R.color.black));
                } else {
                    //make color red
                    holder.unreadMessages.setTextColor(ContextCompat.getColor(context, R.color.red));
                }
            }
        });

        holder.unreadMessages.setText(unreadMessageCount + " new message");

        holder.userName.setText(message.getUserName());

        // Set text color based on the number of unread messages
        if (unreadMessageCount == 0) {
            holder.unreadMessages.setTextColor(ContextCompat.getColor(context, R.color.black));
        } else {
            //make color red
            holder.unreadMessages.setTextColor(ContextCompat.getColor(context, R.color.red));
        }

        Picasso.get().load(message.getMentor().getprofilePictureUrl()).into(holder.otherUserImage);


        // Set OnClickListener on the card
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Mentor mentor = message.getMentor();

                // Create an intent to start the MentorChatActivity
                Intent intent = new Intent(context, MentorChatActivity.class);

                // Pass the Mentor object to the MentorChatActivity
                intent.putExtra("mentor", mentor);

                // Start the activity
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView otherUserImage;
        TextView userName;
        TextView unreadMessages;
        View dividerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            otherUserImage = itemView.findViewById(R.id.otherUserImage);
            userName = itemView.findViewById(R.id.userName);
            unreadMessages = itemView.findViewById(R.id.unreadMessages);
            dividerView = itemView.findViewById(R.id.view);
        }
    }
}

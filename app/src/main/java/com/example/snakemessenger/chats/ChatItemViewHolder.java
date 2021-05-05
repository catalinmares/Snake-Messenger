package com.example.snakemessenger.chats;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snakemessenger.R;
import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

class ChatItemViewHolder extends RecyclerView.ViewHolder {
    private final CircleImageView userProfilePictureImageView;
    private final ImageView userStatusImageView;
    private final TextView userProfileNameTextView;
    private final EmojiconTextView lastMessageTextView;
    private final TextView timestampTextView;

    public ChatItemViewHolder(@NonNull View itemView) {
        super(itemView);

        userProfilePictureImageView = itemView.findViewById(R.id.contact_image_item);
        userProfileNameTextView = itemView.findViewById(R.id.contact_name_item);
        userStatusImageView = itemView.findViewById(R.id.status);
        lastMessageTextView = itemView.findViewById(R.id.contact_status_item);
        timestampTextView = itemView.findViewById(R.id.timestamp);
    }

    public CircleImageView getUserProfilePictureImageView() {
        return userProfilePictureImageView;
    }

    public ImageView getUserStatusImageView() {
        return userStatusImageView;
    }

    public TextView getUserProfileNameTextView() {
        return userProfileNameTextView;
    }

    public EmojiconTextView getLastMessageTextView() {
        return lastMessageTextView;
    }

    public TextView getTimestampTextView() {
        return timestampTextView;
    }
}

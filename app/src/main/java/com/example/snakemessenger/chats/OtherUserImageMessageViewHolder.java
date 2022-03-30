package com.example.snakemessenger.chats;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtherUserImageMessageViewHolder extends RecyclerView.ViewHolder {
    private final CircleImageView senderProfilePictureImageView;
    private final TextView senderNameTextView;
    private final ImageView messageContentImageView;
    private final TextView timestampTextView;
    private final ImageView messageStatusImageView;

    public OtherUserImageMessageViewHolder(@NonNull View itemView) {
        super(itemView);

        senderProfilePictureImageView = itemView.findViewById(R.id.sender_profile_pic);
        senderNameTextView = itemView.findViewById(R.id.sender_name);
        messageContentImageView = itemView.findViewById(R.id.message_content);
        timestampTextView = itemView.findViewById(R.id.message_timestamp);
        messageStatusImageView = itemView.findViewById(R.id.message_status);
    }

    public CircleImageView getSenderProfilePictureImageView() {
        return senderProfilePictureImageView;
    }

    public TextView getSenderNameTextView() {
        return senderNameTextView;
    }

    public ImageView getMessageContentImageView() {
        return messageContentImageView;
    }

    public TextView getTimestampTextView() {
        return timestampTextView;
    }

    public ImageView getMessageStatusImageView() {
        return messageStatusImageView;
    }
}

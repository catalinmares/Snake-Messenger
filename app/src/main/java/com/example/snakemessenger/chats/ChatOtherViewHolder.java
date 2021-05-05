package com.example.snakemessenger.chats;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snakemessenger.R;
import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

class ChatOtherViewHolder extends RecyclerView.ViewHolder {
    private final CircleImageView senderProfilePictureImageView;
    private final TextView senderNameTextView;
    private final EmojiconTextView messageContentTextView;
    private final TextView timestampTextView;
    private final ImageView messageStatusImageView;

    public ChatOtherViewHolder(@NonNull View itemView) {
        super(itemView);

        senderProfilePictureImageView = itemView.findViewById(R.id.sender_profile_pic);
        senderNameTextView = itemView.findViewById(R.id.sender_name);
        messageContentTextView = itemView.findViewById(R.id.message_content);
        timestampTextView = itemView.findViewById(R.id.message_timestamp);
        messageStatusImageView = itemView.findViewById(R.id.message_status);
    }

    public CircleImageView getSenderProfilePictureImageView() {
        return senderProfilePictureImageView;
    }

    public TextView getSenderNameTextView() {
        return senderNameTextView;
    }

    public EmojiconTextView getMessageContentTextView() {
        return messageContentTextView;
    }

    public TextView getTimestampTextView() {
        return timestampTextView;
    }

    public ImageView getMessageStatusImageView() {
        return messageStatusImageView;
    }
}

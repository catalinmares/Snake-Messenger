package com.example.snakemessenger;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

class ChatUserViewHolder extends RecyclerView.ViewHolder {
    private CircleImageView mSenderProfilePicture;
    private TextView mSenderName;
    private TextView mMessageContent;
    private TextView mTimestamp;

    public ChatUserViewHolder(@NonNull View itemView) {
        super(itemView);

        mSenderProfilePicture = itemView.findViewById(R.id.sender_profile_pic);
        mSenderName = itemView.findViewById(R.id.sender_name);
        mMessageContent = itemView.findViewById(R.id.message_content);
        mTimestamp = itemView.findViewById(R.id.message_timestamp);
    }

    public CircleImageView getSenderProfilePicture() {
        return mSenderProfilePicture;
    }

    public TextView getSenderName() {
        return mSenderName;
    }

    public TextView getMessageContent() {
        return mMessageContent;
    }

    public TextView getTimestamp() {
        return mTimestamp;
    }
}

package com.example.snakemessenger;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

class ChatOtherViewHolder extends RecyclerView.ViewHolder {
    private CircleImageView mSenderProfilePicture;
    private TextView mSenderName;
    private TextView mMessageContent;
    private TextView mTimestamp;
    private ImageView mMessageStatus;

    public ChatOtherViewHolder(@NonNull View itemView) {
        super(itemView);

        mSenderProfilePicture = itemView.findViewById(R.id.sender_profile_pic);
        mSenderName = itemView.findViewById(R.id.sender_name);
        mMessageContent = itemView.findViewById(R.id.message_content);
        mTimestamp = itemView.findViewById(R.id.message_timestamp);
        mMessageStatus = itemView.findViewById(R.id.message_status);
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

    public ImageView getMessageStatus() {
        return mMessageStatus;
    }
}

package com.example.snakemessenger;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

class GroupChatViewHolder extends RecyclerView.ViewHolder {
    private CircleImageView mSenderProfilePicture;
    private TextView mSenderName;
    private TextView mMessageContent;
    private TextView mTimestamp;

    public GroupChatViewHolder(@NonNull View itemView) {
        super(itemView);

        mSenderProfilePicture = itemView.findViewById(R.id.sender_profile_pic);
        mSenderName = itemView.findViewById(R.id.sender_name);
        mMessageContent = itemView.findViewById(R.id.message_content);
        mTimestamp = itemView.findViewById(R.id.message_timestamp);
    }

    public CircleImageView getmSenderProfilePicture() {
        return mSenderProfilePicture;
    }

    public TextView getmSenderName() {
        return mSenderName;
    }

    public TextView getmMessageContent() {
        return mMessageContent;
    }

    public TextView getmTimestamp() {
        return mTimestamp;
    }
}

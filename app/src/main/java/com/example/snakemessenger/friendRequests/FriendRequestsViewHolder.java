package com.example.snakemessenger.friendRequests;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.R;

import de.hdodenhof.circleimageview.CircleImageView;

class FriendRequestsViewHolder extends RecyclerView.ViewHolder {
    private CircleImageView mContactPicture;
    private TextView mFriendRequestMessage;
    private TextView mFriendRequestStatus;
    private TextView mFriendRequestTimestamp;

    public FriendRequestsViewHolder(@NonNull View itemView) {
        super(itemView);

        mContactPicture = itemView.findViewById(R.id.friend_request_image_item);
        mFriendRequestMessage = itemView.findViewById(R.id.friend_request_name_item);
        mFriendRequestStatus = itemView.findViewById(R.id.friend_request_status_item);
        mFriendRequestTimestamp = itemView.findViewById(R.id.friend_request_timestamp);
    }

    public CircleImageView getmContactPicture() {
        return mContactPicture;
    }

    public TextView getmFriendRequestMessage() {
        return mFriendRequestMessage;
    }

    public TextView getmFriendRequestStatus() {
        return mFriendRequestStatus;
    }

    public TextView getmFriendRequestTimestamp() {
        return mFriendRequestTimestamp;
    }
}

package com.example.snakemessenger.connectionRequests;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.R;

import de.hdodenhof.circleimageview.CircleImageView;

class ConnectionRequestsViewHolder extends RecyclerView.ViewHolder {
    private CircleImageView mContactPicture;
    private TextView mFriendRequestMessage;
    private TextView mFriendRequestTimestamp;

    public ConnectionRequestsViewHolder(@NonNull View itemView) {
        super(itemView);

        mContactPicture = itemView.findViewById(R.id.connection_request_image_item);
        mFriendRequestMessage = itemView.findViewById(R.id.connection_request_name_item);
        mFriendRequestTimestamp = itemView.findViewById(R.id.connection_request_timestamp);
    }

    public CircleImageView getContactPicture() {
        return mContactPicture;
    }

    public TextView getConnectionRequestMessage() {
        return mFriendRequestMessage;
    }

    public TextView getConnectionRequestTimestamp() {
        return mFriendRequestTimestamp;
    }
}

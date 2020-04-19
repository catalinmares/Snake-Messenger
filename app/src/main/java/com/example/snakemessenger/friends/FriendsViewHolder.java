package com.example.snakemessenger.friends;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

class FriendsViewHolder extends RecyclerView.ViewHolder {
    private TextView mName, mStatus;
    private CircleImageView mProfilePicture;

    public FriendsViewHolder(View itemView) {
        super(itemView);
    }

    public TextView getTextViewName() {
        return mName;
    }

    public TextView getTextViewStatus() {
        return mStatus;
    }

    public CircleImageView getCircleImageViewProfilePicture() {
        return mProfilePicture;
    }
}

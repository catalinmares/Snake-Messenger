package com.example.snakemessenger.chats;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.R;

import de.hdodenhof.circleimageview.CircleImageView;

class ChatItemViewHolder extends RecyclerView.ViewHolder {
    private CircleImageView mUserProfilePic;
    private TextView mUserProfileName;
    private TextView mUserStatus;

    public ChatItemViewHolder(@NonNull View itemView) {
        super(itemView);

        mUserProfilePic = itemView.findViewById(R.id.chat_image_item);
        mUserProfileName = itemView.findViewById(R.id.chat_name_item);
        mUserStatus = itemView.findViewById(R.id.chat_status_item);
    }

    public CircleImageView getmUserProfilePic() {
        return mUserProfilePic;
    }

    public TextView getmUserProfileName() {
        return mUserProfileName;
    }

    public TextView getmUserStatus() {
        return mUserStatus;
    }
}

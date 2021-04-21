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
    private CircleImageView mUserProfilePic;
    private ImageView mUserStatus;
    private TextView mUserProfileName;
    private EmojiconTextView mLastMessage;
    private TextView mTimestamp;

    public ChatItemViewHolder(@NonNull View itemView) {
        super(itemView);

        mUserProfilePic = itemView.findViewById(R.id.contact_image_item);
        mUserProfileName = itemView.findViewById(R.id.contact_name_item);
        mUserStatus = itemView.findViewById(R.id.status);
        mLastMessage = itemView.findViewById(R.id.contact_status_item);
        mTimestamp = itemView.findViewById(R.id.timestamp);
    }

    public CircleImageView getUserProfilePic() {
        return mUserProfilePic;
    }

    public ImageView getUserStatus() {
        return mUserStatus;
    }

    public TextView getUserProfileName() {
        return mUserProfileName;
    }

    public EmojiconTextView getLastMessage() {
        return mLastMessage;
    }

    public TextView getTimestamp() {
        return mTimestamp;
    }
}

package com.example.snakemessenger;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersViewHolder extends RecyclerView.ViewHolder {
    private CircleImageView mProfilePic;
    private TextView mContactName;
    private TextView mContactStatus;

    public UsersViewHolder(View itemView) {
        super(itemView);

        mProfilePic = itemView.findViewById(R.id.friend_image_item);
        mContactName = itemView.findViewById(R.id.friend_name_item);
        mContactStatus = itemView.findViewById(R.id.friend_status_item);
    }

    public CircleImageView getmProfilePic() {
        return mProfilePic;
    }

    public TextView getmContactName() {
        return mContactName;
    }

    public TextView getmContactStatus() {
        return mContactStatus;
    }
}

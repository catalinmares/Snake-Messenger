package com.example.snakemessenger.friends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.R;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsViewHolder> {
    private Context mContext;
    private List<Friend> mFriends;

    public FriendsAdapter(Context mContext, List<Friend> mFriends) {
        this.mContext = mContext;
        this.mFriends = mFriends;
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.friend_item, parent, false);

        return new FriendsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
        Friend currentFriend = mFriends.get(position);

        holder.getTextViewName().setText(currentFriend.getName());
        holder.getTextViewStatus().setText(currentFriend.getStatus());
        holder.getCircleImageViewProfilePicture().setImageResource(R.drawable.profile_image);
    }

    @Override
    public int getItemCount() {
        return mFriends.size();
    }
}

package com.example.snakemessenger.groups;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.R;

import java.util.List;

class GroupsAdapter extends RecyclerView.Adapter<GroupsViewHolder> {
    private Context mContext;
    private List<Group> mGroups;

    public GroupsAdapter(Context mContext, List<Group> mGroups) {
        this.mContext = mContext;
        this.mGroups = mGroups;
    }

    @NonNull
    @Override
    public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.group_item, parent, false);

        return new GroupsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsViewHolder holder, int position) {
        Group currentGroup = mGroups.get(position);

        holder.getTextViewName().setText(currentGroup.getName());
        holder.getCircleImageViewGroup().setImageResource(R.drawable.group_image);
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }
}

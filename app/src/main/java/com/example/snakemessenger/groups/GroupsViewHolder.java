package com.example.snakemessenger.groups;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.R;

import de.hdodenhof.circleimageview.CircleImageView;

class GroupsViewHolder extends RecyclerView.ViewHolder{
    private TextView mGroupName;
    private CircleImageView mGroupImage;

    public GroupsViewHolder(@NonNull View itemView) {
        super(itemView);

        mGroupName = itemView.findViewById(R.id.group_name_item);
        mGroupImage = itemView.findViewById(R.id.group_image_item);
    }

    public TextView getTextViewName() {
        return mGroupName;
    }

    public CircleImageView getCircleImageViewGroup() {
        return mGroupImage;
    }
}

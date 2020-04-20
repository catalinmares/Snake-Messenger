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
    private TextView mGroupDescrition;

    public GroupsViewHolder(@NonNull View itemView) {
        super(itemView);

        mGroupName = itemView.findViewById(R.id.group_name_item);
        mGroupDescrition = itemView.findViewById(R.id.group_description_item);
        mGroupImage = itemView.findViewById(R.id.group_image_item);
    }

    public TextView getTextViewName() {
        return mGroupName;
    }

    public TextView getTextViewDescription() {
        return mGroupDescrition;
    }

    public CircleImageView getCircleImageViewGroup() {
        return mGroupImage;
    }
}

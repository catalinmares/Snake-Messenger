package com.example.snakemessenger.groups;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

class GroupsAdapter extends RecyclerView.Adapter<GroupsViewHolder> {
    private Context mContext;
    private List<Group> mGroups;
    private StorageReference storageReference;

    public GroupsAdapter(Context mContext, List<Group> mGroups) {
        this.mContext = mContext;
        this.mGroups = mGroups;
    }

    @NonNull
    @Override
    public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.group_item, parent, false);

        storageReference = FirebaseStorage.getInstance().getReference();

        return new GroupsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupsViewHolder holder, int position) {
        Group currentGroup = mGroups.get(position);

        String groupName = currentGroup.getName();
        String groupAdmin = currentGroup.getAdminID();
        String groupDescription = currentGroup.getDescription();
        String groupPicture = currentGroup.getPicture();

        holder.getTextViewName().setText(groupName);
        holder.getTextViewDescription().setText(groupDescription);

        if (groupPicture.equals("yes")) {
            final long ONE_MEGABYTE = 1024 * 1024;
            storageReference.child(groupName + "-" + groupAdmin + "-profile_pic")
                    .getBytes(ONE_MEGABYTE)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            holder.getCircleImageViewGroup().setImageBitmap(bitmap);
                        }
                    });
        } else {
            holder.getCircleImageViewGroup().setImageResource(R.drawable.group_image);
        }
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }
}

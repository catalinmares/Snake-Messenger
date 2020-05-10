package com.example.snakemessenger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

class UsersAdapter extends RecyclerView.Adapter<UsersViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    private StorageReference storageReference;

    public UsersAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_item, parent, false);

        storageReference = FirebaseStorage.getInstance().getReference();

        return new UsersViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final UsersViewHolder holder, int position) {
        User currentUser = mUsers.get(position);

        holder.getmContactName().setText(currentUser.getName());
        holder.getmContactStatus().setText(currentUser.getStatus());

        if (currentUser.getPicture()) {
            final long TEN_MEGABYTES = 10 * 1024 * 1024;

            storageReference.child(currentUser.getUserID() + "-profile_pic")
                    .getBytes(TEN_MEGABYTES)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            holder.getmProfilePic().setImageBitmap(bitmap);
                        }
                    });
        } else {
            holder.getmProfilePic().setImageResource(R.drawable.profile_image);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}

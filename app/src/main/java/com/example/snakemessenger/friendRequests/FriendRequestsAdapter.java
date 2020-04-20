package com.example.snakemessenger.friendRequests;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.snakemessenger.User;
import com.example.snakemessenger.UsersViewHolder;

import java.util.List;

class FriendRequestsAdapter extends RecyclerView.Adapter<UsersViewHolder> {
    private Context mContext;
    private List<FriendRequest> friendRequests;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    public FriendRequestsAdapter(Context mContext, List<FriendRequest> friendRequests) {
        this.mContext = mContext;
        this.friendRequests = friendRequests;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_item, parent, false);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        return new UsersViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final UsersViewHolder holder, int position) {
        FriendRequest currentFriendRequest = friendRequests.get(position);

        String userID = currentFriendRequest.getUserID();

        db.collection("users")
                .document(userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);

                        holder.getmContactName().setText(user.getName());
                        holder.getmContactStatus().setText("sent you a friend request");

                        if (user.getPicture().equals("yes")) {
                            final long ONE_MEGABYTE = 1024 * 1024;

                            storageReference.child(user.getUserID() + "-profile_pic")
                                    .getBytes(ONE_MEGABYTE)
                                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            holder.getmProfilePic().setImageBitmap(bitmap);
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
    }
}

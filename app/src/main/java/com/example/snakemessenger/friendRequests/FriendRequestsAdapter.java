package com.example.snakemessenger.friendRequests;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.snakemessenger.User;

import java.util.List;

class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsViewHolder> {
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
    public FriendRequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.friend_request_item, parent, false);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        return new FriendRequestsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendRequestsViewHolder holder, int position) {
        final FriendRequest currentFriendRequest = friendRequests.get(position);

        switch (currentFriendRequest.getStatus()) {
            case "pending":
                holder.getmFriendRequestStatus().setTextColor(Color.parseColor("#BBBB00"));
                holder.getmFriendRequestStatus().setText("Request pending");
                break;
            case "accepted":
                holder.getmFriendRequestStatus().setTextColor(Color.parseColor("#00BB00"));
                holder.getmFriendRequestStatus().setText("Request accepted");
                break;
            case "deleted":
                holder.getmFriendRequestStatus().setTextColor(Color.parseColor("#BB0000"));
                holder.getmFriendRequestStatus().setText("Request deleted");
                break;
            default:
                break;
        }
        holder.getmFriendRequestTimestamp().setText(
                String.format(
                        "%s, %s",
                        currentFriendRequest.getDate(),
                        currentFriendRequest.getTime()
                )
        );

        String userID = currentFriendRequest.getUserID();

        db.collection("users")
                .document(userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);

                        holder.getmFriendRequestMessage().setText(user.getName());

                        if (user.getPicture().equals("yes")) {
                            final long ONE_MEGABYTE = 1024 * 1024;

                            storageReference.child(user.getUserID() + "-profile_pic")
                                    .getBytes(ONE_MEGABYTE)
                                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            holder.getmContactPicture().setImageBitmap(bitmap);
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

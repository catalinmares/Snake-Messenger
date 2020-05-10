package com.example.snakemessenger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

class FriendsAdapter extends RecyclerView.Adapter<UsersViewHolder> {
    private Context mContext;
    private List<String> mFriends;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    public FriendsAdapter(Context context, List<String> friends) {
        this.mContext = context;
        this.mFriends = friends;
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
        final String currentFriendID = mFriends.get(position);

        db.collection("users")
                .document(currentFriendID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User currentUser = documentSnapshot.toObject(User.class);

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
                            }
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mFriends.size();
    }
}

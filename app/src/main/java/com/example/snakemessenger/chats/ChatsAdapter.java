package com.example.snakemessenger.chats;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.R;
import com.example.snakemessenger.SettingsActivity;
import com.example.snakemessenger.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class ChatsAdapter extends RecyclerView.Adapter<ChatItemViewHolder> {
    private Context mContext;
    private List<Chat> mChats;

    private FirebaseFirestore db;
    private StorageReference storageReference;
    private HashMap<String, Bitmap> profilePictures;

    public ChatsAdapter(Context mContext, List<Chat> mChats) {
        this.mContext = mContext;
        this.mChats = mChats;
    }


    @NonNull
    @Override
    public ChatItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.chat_item, parent, false);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        return new ChatItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatItemViewHolder holder, int position) {
        final Chat currentChat = mChats.get(position);

        db.collection("users")
                .document(currentChat.getUserID())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            final User currentUser = documentSnapshot.toObject(User.class);

                            holder.getmUserProfileName().setText(currentUser.getName());
                            holder.getmUserStatus().setText(currentUser.getStatus());

                            if (currentUser.getPicture().equals("yes")) {
                                final long ONE_MEGABYTE = 1024 * 1024;
                                if (MainActivity.profilePictures.containsKey(currentUser.getUserID())) {
                                    holder.getmUserProfilePic().setImageBitmap(MainActivity.profilePictures.get(currentUser.getUserID()));
                                } else {
                                    storageReference.child(currentUser.getUserID() + "-profile_pic")
                                            .getBytes(ONE_MEGABYTE)
                                            .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                @Override
                                                public void onSuccess(byte[] bytes) {
                                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                    holder.getmUserProfilePic().setImageBitmap(bitmap);
                                                    MainActivity.profilePictures.put(currentUser.getUserID(), bitmap);
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }
}

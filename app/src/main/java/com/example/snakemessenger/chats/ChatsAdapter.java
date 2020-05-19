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
import com.example.snakemessenger.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

class ChatsAdapter extends RecyclerView.Adapter<ChatItemViewHolder> {
    private Context mContext;
    private List<Chat> mChats;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    public ChatsAdapter(Context mContext, List<Chat> mChats) {
        this.mContext = mContext;
        this.mChats = mChats;
    }


    @NonNull
    @Override
    public ChatItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.chat_item, parent, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        return new ChatItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatItemViewHolder holder, int position) {
        final Chat currentChat = mChats.get(position);
        final List<String> users = currentChat.getUsers();
        users.remove(currentUser.getUid());

        db.collection("users")
                .document(users.get(0))
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            final User user = documentSnapshot.toObject(User.class);

                            holder.getmUserProfileName().setText(user.getName());

                            String chatID = users.get(0).compareTo(currentUser.getUid()) > 0 ?
                                    users.get(0).concat(currentUser.getUid()) :
                                    currentUser.getUid().concat(users.get(0));

                            db.collection("conversations")
                                    .document(chatID)
                                    .collection("messages")
                                    .orderBy("timestamp", Query.Direction.DESCENDING)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if (queryDocumentSnapshots.isEmpty()) {
                                                holder.getmUserStatus().setText(user.getStatus());
                                            } else {
                                                List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                                                DocumentSnapshot doc = docs.get(0);

                                                String senderID = doc.getString("sender");
                                                String content = doc.getString("content");

                                                if (senderID.equals(currentUser.getUid())) {
                                                    if (content.length() > 30) {
                                                        holder.getmUserStatus().setText(
                                                                String.format(
                                                                        "You: %s...",
                                                                        content.substring(0, Math.min(30, content.length())))
                                                        );
                                                    } else {
                                                        holder.getmUserStatus().setText(
                                                                String.format(
                                                                        "You: %s",
                                                                        content.substring(0, Math.min(30, content.length())))
                                                        );
                                                    }
                                                } else {
                                                    if (content.length() > 30) {
                                                        holder.getmUserStatus().setText(
                                                                String.format(
                                                                        "%s: %s...",
                                                                        user.getName(), content.substring(0, Math.min(30, content.length())))
                                                        );
                                                    } else {
                                                        holder.getmUserStatus().setText(
                                                                String.format(
                                                                        "%s: %s",
                                                                        user.getName(), content.substring(0, Math.min(30, content.length())))
                                                        );
                                                    }
                                                }
                                            }
                                        }
                                    });

                            if (user.getPicture()) {
                                final long TEN_MEGABYTES = 10 * 1024 * 1024;
                                if (MainActivity.profilePictures.containsKey(user.getUserID())) {
                                    holder.getmUserProfilePic().setImageBitmap(MainActivity.profilePictures.get(user.getUserID()));
                                } else {
                                    storageReference.child(user.getUserID() + "-profile_pic")
                                            .getBytes(TEN_MEGABYTES)
                                            .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                @Override
                                                public void onSuccess(byte[] bytes) {
                                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                    holder.getmUserProfilePic().setImageBitmap(bitmap);
                                                    MainActivity.profilePictures.put(user.getUserID(), bitmap);
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

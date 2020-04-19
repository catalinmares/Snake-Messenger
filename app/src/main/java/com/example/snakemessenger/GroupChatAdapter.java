package com.example.snakemessenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatViewHolder> {
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private List<GroupMessage> mMessages;

    public GroupChatAdapter(List<GroupMessage> mMessages) {
        this.mMessages = mMessages;
    }

    @NonNull
    @Override
    public GroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message_item, parent, false);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        return new GroupChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupChatViewHolder holder, int position) {
        GroupMessage currentMessage = mMessages.get(position);

        final String senderID = currentMessage.getSenderID();
        String messageContent = currentMessage.getContent();
        String date = currentMessage.getDate();
        String time = currentMessage.getTime();

        db.collection("users")
                .document(senderID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            holder.getmSenderName().setText(documentSnapshot.getString("name"));
                            String hasPhoto = documentSnapshot.getString("picture");

                            assert hasPhoto != null;
                            if (hasPhoto.equals("yes")) {
                                final long ONE_MEGABYTE = 1024 * 1024;

                                storageReference.child(senderID + "-profile_pic")
                                        .getBytes(ONE_MEGABYTE)
                                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                holder.getmSenderProfilePicture().setImageBitmap(bitmap);
                                            }
                                        });
                            }
                        }
                    }
                });

        holder.getmMessageContent().setText(messageContent);
        holder.getmTimestamp().setText(String.format("%s, %s", date, time));
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }
}

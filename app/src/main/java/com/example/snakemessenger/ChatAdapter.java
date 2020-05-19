package com.example.snakemessenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private List<Message> mMessages;

    public ChatAdapter(List<Message> mMessages) {
        this.mMessages = mMessages;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = mMessages.get(position);

        if (!msg.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return 0;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        switch (viewType) {
            case 0:
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_message_item, parent, false);

                return new ChatUserViewHolder(itemView);
            case 1:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_message_item2, parent, false);

                return new ChatOtherViewHolder(itemView);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message currentMessage = mMessages.get(position);

        final String senderID = currentMessage.getSender();
        String messageContent = currentMessage.getContent();
        Date date = currentMessage.getTimestamp().toDate();
        SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yy ',' HH:mm");

        switch (getItemViewType(position)) {
            case 0:
                final ChatUserViewHolder mHolder = (ChatUserViewHolder) holder;

                db.collection("users")
                        .document(senderID)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    mHolder.getmSenderName().setText(documentSnapshot.getString("name"));
                                    boolean hasPhoto = documentSnapshot.getBoolean("picture");

                                    if (hasPhoto) {
                                        final long TEN_MEGABYTES = 10 * 1024 * 1024;
                                        if (MainActivity.profilePictures.containsKey(senderID)) {
                                            mHolder.getmSenderProfilePicture().setImageBitmap(MainActivity.profilePictures.get(senderID));
                                        } else {
                                            storageReference.child(senderID + "-profile_pic")
                                                    .getBytes(TEN_MEGABYTES)
                                                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                        @Override
                                                        public void onSuccess(byte[] bytes) {
                                                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                            mHolder.getmSenderProfilePicture().setImageBitmap(bitmap);
                                                            MainActivity.profilePictures.put(senderID, bitmap);
                                                        }
                                                    });
                                        }
                                    }
                                }
                            }
                        });

                mHolder.getmMessageContent().setText(messageContent);
                mHolder.getmTimestamp().setText(ft.format(date));
                break;

            case 1:
                final ChatOtherViewHolder nHolder = (ChatOtherViewHolder) holder;

                db.collection("users")
                        .document(senderID)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    nHolder.getmSenderName().setText(documentSnapshot.getString("name"));
                                    boolean hasPhoto = documentSnapshot.getBoolean("picture");

                                    if (hasPhoto) {
                                        final long TEN_MEGABYTES = 10 * 1024 * 1024;
                                        if (MainActivity.profilePictures.containsKey(senderID)) {
                                            nHolder.getmSenderProfilePicture().setImageBitmap(MainActivity.profilePictures.get(senderID));
                                        } else {
                                            storageReference.child(senderID + "-profile_pic")
                                                    .getBytes(TEN_MEGABYTES)
                                                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                        @Override
                                                        public void onSuccess(byte[] bytes) {
                                                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                            nHolder.getmSenderProfilePicture().setImageBitmap(bitmap);
                                                            MainActivity.profilePictures.put(senderID, bitmap);
                                                        }
                                                    });
                                        }
                                    }
                                }
                            }
                        });

                nHolder.getmMessageContent().setText(messageContent);
                nHolder.getmTimestamp().setText(ft.format(date));
                break;

            default:
                break;
        }

    }

//    @Override
//    public void onBindViewHolder(@NonNull final ChatUserViewHolder holder, int position) {
//        Message currentMessage = mMessages.get(position);
//
//        final String senderID = currentMessage.getSender();
//        String messageContent = currentMessage.getContent();
//        Date date = currentMessage.getTimestamp().toDate();
//        SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yy ',' HH:mm");
//
//        db.collection("users")
//                .document(senderID)
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if (documentSnapshot.exists()) {
//                            holder.getmSenderName().setText(documentSnapshot.getString("name"));
//                            boolean hasPhoto = documentSnapshot.getBoolean("picture");
//
//                            if (hasPhoto) {
//                                final long TEN_MEGABYTES = 10 * 1024 * 1024;
//                                if (MainActivity.profilePictures.containsKey(senderID)) {
//                                    holder.getmSenderProfilePicture().setImageBitmap(MainActivity.profilePictures.get(senderID));
//                                } else {
//                                    storageReference.child(senderID + "-profile_pic")
//                                            .getBytes(TEN_MEGABYTES)
//                                            .addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                                                @Override
//                                                public void onSuccess(byte[] bytes) {
//                                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                                                    holder.getmSenderProfilePicture().setImageBitmap(bitmap);
//                                                    MainActivity.profilePictures.put(senderID, bitmap);
//                                                }
//                                            });
//                                }
//                            }
//                        }
//                    }
//                });
//
//        holder.getmMessageContent().setText(messageContent);
//        holder.getmTimestamp().setText(ft.format(date));
//    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }
}

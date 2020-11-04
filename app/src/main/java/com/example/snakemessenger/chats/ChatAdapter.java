package com.example.snakemessenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.database.Contact;
import com.example.snakemessenger.database.Message;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<Message> mMessages;

    public ChatAdapter(Context context, List<Message> messages) {
        this.mContext = context;
        this.mMessages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = mMessages.get(position);

        int status = message.getStatus();

        if (status == Message.RECEIVED) {
            return 0;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

        String messageContent = currentMessage.getContent();
        Date date = currentMessage.getTimestamp();
        SimpleDateFormat ft = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        Date currentDate = Calendar.getInstance().getTime();

        switch (getItemViewType(position)) {
            case 0:
                ChatUserViewHolder mHolder = (ChatUserViewHolder) holder;

                Contact sender = MainActivity.db.getContactDao().findByPhone(currentMessage.getToFrom());

                if (sender.getPhotoUri() != null) {
                    Uri imageUri = Uri.parse(sender.getPhotoUri());

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imageUri);
                        mHolder.getSenderProfilePicture().setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                mHolder.getSenderName().setText(sender.getName());
                mHolder.getMessageContent().setText(messageContent);
                mHolder.getTimestamp().setText(DateManager.getLastActiveText(ft.format(currentDate), ft.format(date)));
                break;

            case 1:
                ChatOtherViewHolder nHolder = (ChatOtherViewHolder) holder;

                SharedPreferences sharedPreferences = mContext.getSharedPreferences("LOGIN_DETAILS", MODE_PRIVATE);

                String name = sharedPreferences.getString("name", "");
                String photoUri = sharedPreferences.getString("photoUri", null);

                if (photoUri != null) {
                    Uri imageUri = Uri.parse(photoUri);

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imageUri);
                        nHolder.getSenderProfilePicture().setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                nHolder.getSenderName().setText(name);
                nHolder.getMessageContent().setText(messageContent);
                nHolder.getTimestamp().setText(DateManager.getLastActiveText(ft.format(currentDate), ft.format(date)));

                if (currentMessage.getStatus() == Message.SENT) {
                    nHolder.getMessageStatus().setImageResource(R.drawable.ic_baseline_done_24);
                } else {
                    nHolder.getMessageStatus().setImageResource(R.drawable.ic_baseline_done_all_24);
                }

                break;

            default:
                break;
        }

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }
}

package com.example.snakemessenger.chats;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snakemessenger.managers.DateManager;
import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.R;
import com.example.snakemessenger.database.Contact;
import com.example.snakemessenger.database.Message;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<Message> messages;
    private Contact contact;

    public ChatAdapter(Context context, List<Message> messages, Contact contact) {
        this.mContext = context;
        this.messages = messages;
        this.contact = contact;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        int status = message.getStatus();

        if (status == Message.RECEIVED) {
            return 0;
        }

        return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_message_item, parent, false);

            return new ChatUserViewHolder(itemView);
        }

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message_item2, parent, false);

        return new ChatOtherViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message currentMessage = messages.get(position);

        String messageContent = currentMessage.getContent();
        Date date = currentMessage.getTimestamp();
        SimpleDateFormat ft = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);

        Date currentDate = Calendar.getInstance().getTime();

        switch (getItemViewType(position)) {
            case 0:
                ChatUserViewHolder mHolder = (ChatUserViewHolder) holder;

                if (contact.getPhotoUri() != null) {
                    Uri imageUri = Uri.parse(contact.getPhotoUri());
                    Glide.with(mContext).load(imageUri).into(mHolder.getSenderProfilePicture());
                }

                mHolder.getSenderName().setText(contact.getName());
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
                    Glide.with(mContext).load(imageUri).into(nHolder.getSenderProfilePicture());
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
        return messages.size();
    }

    public void setMessages(List<Message> messages) {
        if (this.messages.size() > 0) {
            this.messages.clear();
        }

        this.messages = messages;
        notifyDataSetChanged();
    }

    public void setContact(Contact contact) {
        this.contact = contact;
        notifyDataSetChanged();
    }
}

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
import com.example.snakemessenger.general.Constants;
import com.example.snakemessenger.managers.DateManager;
import com.example.snakemessenger.R;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.models.Message;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private List<Message> messages;
    private Contact contact;

    public ChatAdapter(Context context, List<Message> messages, Contact contact) {
        this.context = context;
        this.messages = messages;
        this.contact = contact;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        int status = message.getStatus();

        if (status == Constants.MESSAGE_STATUS_RECEIVED) {
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
        Date date = new Date(currentMessage.getTimestamp());
        SimpleDateFormat ft = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);

        Date currentDate = Calendar.getInstance().getTime();

        switch (getItemViewType(position)) {
            case 0:
                ChatUserViewHolder mHolder = (ChatUserViewHolder) holder;

                if (contact.getPhotoUri() != null) {
                    Uri imageUri = Uri.parse(contact.getPhotoUri());
                    Glide.with(context).load(imageUri).into(mHolder.getSenderProfilePictureImageView());
                }

                mHolder.getSenderNameTextView().setText(contact.getName());
                mHolder.getMessageContentTextView().setText(messageContent);
                mHolder.getTimestampTextView().setText(DateManager.getLastActiveText(ft.format(currentDate), ft.format(date)));
                break;

            case 1:
                ChatOtherViewHolder nHolder = (ChatOtherViewHolder) holder;

                SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);

                String name = sharedPreferences.getString(Constants.SHARED_PREFERENCES_NAME, "");
                String photoUri = sharedPreferences.getString(Constants.SHARED_PREFERENCES_PHOTO_URI, null);

                if (photoUri != null) {
                    Uri imageUri = Uri.parse(photoUri);
                    Glide.with(context).load(imageUri).into(nHolder.getSenderProfilePictureImageView());
                }

                nHolder.getSenderNameTextView().setText(name);
                nHolder.getMessageContentTextView().setText(messageContent);
                nHolder.getTimestampTextView().setText(DateManager.getLastActiveText(ft.format(currentDate), ft.format(date)));

                if (currentMessage.getStatus() == Constants.MESSAGE_STATUS_SENT) {
                    nHolder.getMessageStatusImageView().setImageResource(R.drawable.ic_baseline_done_24);
                } else {
                    nHolder.getMessageStatusImageView().setImageResource(R.drawable.ic_baseline_done_all_24);
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

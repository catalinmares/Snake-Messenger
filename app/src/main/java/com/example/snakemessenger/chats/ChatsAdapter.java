package com.example.snakemessenger.chats;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.snakemessenger.general.Constants;
import com.example.snakemessenger.managers.DateManager;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.R;
import com.example.snakemessenger.models.Message;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.snakemessenger.MainActivity.db;
import static com.example.snakemessenger.MainActivity.myDeviceId;

class ChatsAdapter extends RecyclerView.Adapter<ChatItemViewHolder> {
    private final Context context;
    private List<Contact> chats;

    public ChatsAdapter(Context context, List<Contact> chats) {
        this.context = context;
        this.chats = chats;
    }

    @NonNull
    @Override
    public ChatItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.contact_item, parent, false);

        return new ChatItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatItemViewHolder holder, int position) {
        Contact currentChatContact = chats.get(position);

        holder.getUserProfileNameTextView().setText(currentChatContact.getName());

        if (currentChatContact.getPhotoUri() != null) {
            Uri imageUri = Uri.parse(currentChatContact.getPhotoUri());
            Glide.with(context).load(imageUri).into(holder.getUserProfilePictureImageView());
        }

        if (currentChatContact.isConnected()) {
            holder.getUserStatusImageView().setVisibility(View.VISIBLE);
        } else {
            holder.getUserStatusImageView().setVisibility(View.GONE);
        }

        Message lastMessage = db.getMessageDao().getLastMessage(myDeviceId, currentChatContact.getDeviceID());
        String messageContent = lastMessage.getContent();

        if (lastMessage.getStatus() == Constants.MESSAGE_STATUS_RECEIVED) {
            holder.getLastMessageTextView().setText(messageContent);
        } else {
            holder.getLastMessageTextView().setText(String.format("You: %s", messageContent));
        }

        Date date = new Date(lastMessage.getTimestamp());
        SimpleDateFormat ft = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);

        Date currentDate = Calendar.getInstance().getTime();

        holder.getTimestampTextView().setText(String.format("~ %s", DateManager.getLastMessageDate(ft.format(currentDate), ft.format(date))));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public void setChats(List<Contact> chats) {
        this.chats = chats;
        notifyDataSetChanged();
    }

    public List<Contact> getChats() {
        return chats;
    }
}

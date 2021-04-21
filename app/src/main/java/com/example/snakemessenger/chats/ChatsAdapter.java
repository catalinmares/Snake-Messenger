package com.example.snakemessenger.chats;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.snakemessenger.managers.DateManager;
import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.database.Contact;
import com.example.snakemessenger.R;
import com.example.snakemessenger.database.Message;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class ChatsAdapter extends RecyclerView.Adapter<ChatItemViewHolder> {
    private Context context;
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

        holder.getUserProfileName().setText(currentChatContact.getName());

        if (currentChatContact.getPhotoUri() != null) {
            Uri imageUri = Uri.parse(currentChatContact.getPhotoUri());
            Glide.with(context).load(imageUri).into(holder.getUserProfilePic());
        }

        holder.getUserProfilePic().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if (currentChatContact.isConnected()) {
            holder.getUserStatus().setVisibility(View.VISIBLE);
        } else {
            holder.getUserStatus().setVisibility(View.GONE);
        }

        Message lastMessage = MainActivity.db.getMessageDao().getLastMessage(currentChatContact.getPhone());
        String messageContent = lastMessage.getContent();

        if (lastMessage.getStatus() == Message.RECEIVED) {
            holder.getLastMessage().setText(messageContent);
        } else {
            holder.getLastMessage().setText(String.format("You: %s", messageContent));
        }

        Date date = lastMessage.getTimestamp();
        SimpleDateFormat ft = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);

        Date currentDate = Calendar.getInstance().getTime();

        holder.getTimestamp().setText(String.format("~ %s", DateManager.getLastMessageDate(ft.format(currentDate), ft.format(date))));
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

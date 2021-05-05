package com.example.snakemessenger.contacts;

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
import com.example.snakemessenger.R;
import com.example.snakemessenger.models.Contact;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsViewHolder> {
    private final Context context;
    private List<Contact> contacts;

    public ContactsAdapter(Context context, List<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.contact_item, parent, false);

        return new ContactsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position) {
        Contact currentContact = contacts.get(position);

        holder.getContactNameTextView().setText(currentContact.getName());

        if (currentContact.isConnected()) {
            holder.getContactStatusTextView().setText(R.string.active_now);
            holder.getStatusImageView().setVisibility(View.VISIBLE);
        } else {
            Date currentTime = new Date(System.currentTimeMillis());
            SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);

            holder.getContactStatusTextView().setText(
                    String.format(
                            "Last seen %s",
                            DateManager.getLastActiveText(
                                    df.format(currentTime),
                                    df.format(new Date(currentContact.getLastActive()))
                            )
                    )
            );

            holder.getStatusImageView().setVisibility(View.GONE);
        }

        if (currentContact.getPhotoUri() != null) {
            Uri imageUri = Uri.parse(currentContact.getPhotoUri());
            Glide.with(context).load(imageUri).into(holder.getProfilePictureImageView());
        }

        holder.getTimestampTextView().setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }
}

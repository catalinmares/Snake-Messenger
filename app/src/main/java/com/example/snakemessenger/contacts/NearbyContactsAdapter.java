package com.example.snakemessenger.contacts;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snakemessenger.R;
import com.example.snakemessenger.models.Contact;

import java.util.List;

public class NearbyContactsAdapter extends RecyclerView.Adapter<ContactsViewHolder> {
    private final Context context;
    private List<Contact> users;

    public NearbyContactsAdapter(Context context, List<Contact> users) {
        this.context = context;
        this.users = users;
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
        Contact currentContact = users.get(position);

        holder.getContactNameTextView().setText(currentContact.getName());

        if (currentContact.getPhotoUri() != null) {
            Glide.with(context).load(Uri.parse(currentContact.getPhotoUri())).into(holder.getProfilePictureImageView());
        } else {
            Glide.with(context).load(R.drawable.profile_image).into(holder.getProfilePictureImageView());
        }

        holder.getTimestampTextView().setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<Contact> users) {
        this.users = users;
        notifyDataSetChanged();
    }
}

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
import com.example.snakemessenger.R;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.models.ContactWrapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomContactsAdapter extends RecyclerView.Adapter<CustomContactViewHolder> {
    private final Context context;
    private final List<ContactWrapper> contactWrappers;

    public CustomContactsAdapter(Context context, List<ContactWrapper> contactWrappers) {
        this.context = context;
        this.contactWrappers = contactWrappers;
    }

    @NonNull
    @Override
    public CustomContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.custom_contact_item, parent, false);

        return new CustomContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomContactViewHolder holder, int position) {
        ContactWrapper contactWrapper = contactWrappers.get(position);
        Contact currentContact = contactWrapper.getContact();

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

        if (contactWrapper.isSelected()) {
            holder.getSelectedCheckbox().setChecked(true);
            holder.getSelectedCheckbox().setVisibility(View.VISIBLE);
        } else {
            holder.getSelectedCheckbox().setChecked(false);
            holder.getSelectedCheckbox().setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return contactWrappers.size();
    }
}

package com.example.snakemessenger.chats;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snakemessenger.managers.DateManager;
import com.example.snakemessenger.R;
import com.example.snakemessenger.database.Contact;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomContactsAdapter extends RecyclerView.Adapter<CustomContactViewHolder> {
    private Context mContext;
    private List<ContactWrapper> mContactWrappers;

    public CustomContactsAdapter(Context mContext, List<ContactWrapper> mContactWrappers) {
        this.mContext = mContext;
        this.mContactWrappers = mContactWrappers;
    }

    @NonNull
    @Override
    public CustomContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.custom_contact_item, parent, false);

        return new CustomContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomContactViewHolder holder, int position) {
        ContactWrapper contactWrapper = mContactWrappers.get(position);
        Contact currentContact = contactWrapper.getContact();

        holder.getContactName().setText(currentContact.getName());

        if (currentContact.isConnected()) {
            holder.getContactStatus().setText(R.string.active_now);
            holder.getStatus().setVisibility(View.VISIBLE);
        } else {
            Date currentTime = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);

            holder.getContactStatus().setText(
                    String.format(
                            "Last seen %s",
                            DateManager.getLastActiveText(
                                    df.format(currentTime),
                                    currentContact.getLastActive()
                            )
                    )
            );

            holder.getStatus().setVisibility(View.GONE);
        }

        if (currentContact.getPhotoUri() != null) {
            Uri imageUri = Uri.parse(currentContact.getPhotoUri());
            Glide.with(mContext).load(imageUri).into(holder.getProfilePic());
        }

        if (contactWrapper.isSelected()) {
            holder.getSelected().setChecked(true);
            holder.getSelected().setVisibility(View.VISIBLE);
        } else {
            holder.getSelected().setChecked(false);
            holder.getSelected().setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mContactWrappers.size();
    }
}

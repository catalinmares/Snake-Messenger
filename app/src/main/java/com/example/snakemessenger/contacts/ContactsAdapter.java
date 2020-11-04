package com.example.snakemessenger;

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

import com.example.snakemessenger.database.Contact;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

class ContactsAdapter extends RecyclerView.Adapter<ContactsViewHolder> {
    private List<Contact> mContacts;
    private Context context;

    public ContactsAdapter(List<Contact> contacts, Context context) {
        this.mContacts = contacts;
        this.context = context;
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
        Contact currentContact = mContacts.get(position);

        holder.getContactName().setText(currentContact.getName());

        if (currentContact.isConnected()) {
            holder.getContactStatus().setText("Active now");
            holder.getStatus().setVisibility(View.VISIBLE);
        } else {
            Date currentTime = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

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

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                holder.getProfilePic().setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(
                        context,
                        "Failed to load image from device.",
                        Toast.LENGTH_SHORT
                ).show();

                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }
}

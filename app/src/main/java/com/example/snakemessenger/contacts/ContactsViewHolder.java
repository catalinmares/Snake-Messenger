package com.example.snakemessenger.contacts;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snakemessenger.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsViewHolder extends RecyclerView.ViewHolder {
    private final CircleImageView profilePictureImageView;
    private final ImageView statusImageView;
    private final TextView contactNameTextView;
    private final TextView contactStatusTextView;
    private final TextView timestampTextView;

    public ContactsViewHolder(View itemView) {
        super(itemView);

        profilePictureImageView = itemView.findViewById(R.id.contact_image_item);
        statusImageView = itemView.findViewById(R.id.status);
        contactNameTextView = itemView.findViewById(R.id.contact_name_item);
        contactStatusTextView = itemView.findViewById(R.id.contact_status_item);
        timestampTextView = itemView.findViewById(R.id.timestamp);
    }

    public CircleImageView getProfilePictureImageView() {
        return profilePictureImageView;
    }

    public ImageView getStatusImageView() {
        return statusImageView;
    }

    public TextView getContactNameTextView() {
        return contactNameTextView;
    }

    public TextView getContactStatusTextView() {
        return contactStatusTextView;
    }

    public TextView getTimestampTextView() {
        return timestampTextView;
    }
}

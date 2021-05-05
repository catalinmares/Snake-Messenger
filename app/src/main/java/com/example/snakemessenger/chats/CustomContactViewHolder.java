package com.example.snakemessenger.chats;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snakemessenger.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class CustomContactViewHolder extends RecyclerView.ViewHolder {
    private final CircleImageView profilePictureImageView;
    private final ImageView statusImageView;
    private final TextView contactNameTextView;
    private final TextView contactStatusTextView;
    private final CheckBox selectedCheckbox;

    public CustomContactViewHolder(@NonNull View itemView) {
        super(itemView);

        profilePictureImageView = itemView.findViewById(R.id.contact_image_item);
        statusImageView = itemView.findViewById(R.id.status);
        contactNameTextView = itemView.findViewById(R.id.contact_name_item);
        contactStatusTextView = itemView.findViewById(R.id.contact_status_item);
        selectedCheckbox = itemView.findViewById(R.id.checkbox);
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

    public CheckBox getSelectedCheckbox() {
        return selectedCheckbox;
    }
}

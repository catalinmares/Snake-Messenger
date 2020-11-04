package com.example.snakemessenger;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsViewHolder extends RecyclerView.ViewHolder {
    private CircleImageView mProfilePic;
    private ImageView mStatus;
    private TextView mContactName;
    private TextView mContactStatus;

    public ContactsViewHolder(View itemView) {
        super(itemView);

        mProfilePic = itemView.findViewById(R.id.contact_image_item);
        mStatus = itemView.findViewById(R.id.status);
        mContactName = itemView.findViewById(R.id.contact_name_item);
        mContactStatus = itemView.findViewById(R.id.contact_status_item);
    }

    public CircleImageView getProfilePic() {
        return mProfilePic;
    }

    public ImageView getStatus() {
        return mStatus;
    }

    public TextView getContactName() {
        return mContactName;
    }

    public TextView getContactStatus() {
        return mContactStatus;
    }
}

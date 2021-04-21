package com.example.snakemessenger.contacts;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snakemessenger.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsViewHolder extends RecyclerView.ViewHolder {
    private CircleImageView mProfilePic;
    private ImageView mStatus;
    private TextView mContactName;
    private TextView mContactStatus;
    private TextView mTimestamp;

    public ContactsViewHolder(View itemView) {
        super(itemView);

        mProfilePic = itemView.findViewById(R.id.contact_image_item);
        mStatus = itemView.findViewById(R.id.status);
        mContactName = itemView.findViewById(R.id.contact_name_item);
        mContactStatus = itemView.findViewById(R.id.contact_status_item);
        mTimestamp = itemView.findViewById(R.id.timestamp);
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

    public TextView getTimestamp() {
        return mTimestamp;
    }
}

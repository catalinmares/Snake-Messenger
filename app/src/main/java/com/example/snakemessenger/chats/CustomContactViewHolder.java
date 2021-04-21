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
    private CircleImageView mProfilePic;
    private ImageView mStatus;
    private TextView mContactName;
    private TextView mContactStatus;
    private CheckBox mSelected;

    public CustomContactViewHolder(@NonNull View itemView) {
        super(itemView);

        mProfilePic = itemView.findViewById(R.id.contact_image_item);
        mStatus = itemView.findViewById(R.id.status);
        mContactName = itemView.findViewById(R.id.contact_name_item);
        mContactStatus = itemView.findViewById(R.id.contact_status_item);
        mSelected = itemView.findViewById(R.id.checkbox);
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

    public CheckBox getSelected() {
        return mSelected;
    }
}

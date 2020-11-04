package com.example.snakemessenger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snakemessenger.contacts.ContactsViewHolder;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<ContactsViewHolder> {
    private Context mContext;
    private List<User> mUsers;

    public UsersAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.contact_item, parent, false);

        return new ContactsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position) {
        User currentUser = mUsers.get(position);

        holder.getContactName().setText(currentUser.getPhone());
        holder.getProfilePic().setImageResource(R.drawable.profile_image);
        holder.getTimestamp().setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}

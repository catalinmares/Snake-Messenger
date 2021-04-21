package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.snakemessenger.chats.ChatsFragment;
import com.example.snakemessenger.contacts.ContactsFragment;

public class TabsAccessorAdapter extends FragmentStateAdapter {

    public TabsAccessorAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new ChatsFragment();
        }
        return new ContactsFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

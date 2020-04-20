package com.example.snakemessenger.groups;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.CreateGroupActivity;
import com.example.snakemessenger.GroupChatActivity;
import com.example.snakemessenger.RecyclerTouchListener;
import com.example.snakemessenger.R;
import com.example.snakemessenger.RecyclerViewClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {
    private View groupFragmentView;
    private RecyclerView mGroupsRecyclerView;
    private GroupsAdapter groupsAdapter;
    private List<Group> groups;
    private FloatingActionButton mCreateGroup;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    public GroupsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        mCreateGroup = groupFragmentView.findViewById(R.id.create_group_btn);
        mCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestNewGroup();
            }
        });

        mGroupsRecyclerView = groupFragmentView.findViewById(R.id.groups_recycler_view);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mGroupsRecyclerView.setLayoutManager(layoutManager);

        db.collection("groups")
                .whereArrayContains("users", currentUser.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        groups = queryDocumentSnapshots.toObjects(Group.class);
                        groupsAdapter = new GroupsAdapter(getContext(), groups);
                        mGroupsRecyclerView.setAdapter(groupsAdapter);
                    }
        });

        mGroupsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mGroupsRecyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                sendUserToGroupConversation(groups.get(position));
            }

            @Override
            public void onLongClick(View child, int position) {

            }
        }));

        return groupFragmentView;
    }

    private void requestNewGroup() {
        Intent createGroupIntent = new Intent(getActivity(), CreateGroupActivity.class);
        startActivity(createGroupIntent);
    }

    private void sendUserToGroupConversation(Group group) {
        String groupName = group.getName();
        String adminID = group.getAdminID();
        String groupDescription = group.getDescription();
        String groupPic = group.getPicture();

        Intent groupChatIntent = new Intent(getActivity(), GroupChatActivity.class);
        groupChatIntent.putExtra("name", groupName);
        groupChatIntent.putExtra("adminID", adminID);
        groupChatIntent.putExtra("description", groupDescription);
        groupChatIntent.putExtra("picture", groupPic);
        startActivity(groupChatIntent);
    }
}

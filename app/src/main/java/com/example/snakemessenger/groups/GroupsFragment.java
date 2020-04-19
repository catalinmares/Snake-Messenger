package com.example.snakemessenger.groups;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.GroupChatActivity;
import com.example.snakemessenger.GroupsRecyclerTouchListener;
import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.R;
import com.example.snakemessenger.RecyclerViewClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


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
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        mCreateGroup = groupFragmentView.findViewById(R.id.create_group_btn);
        mCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestNewGroup();
            }
        });

        initView();
        setLayoutManager();
        getGroups();

        mGroupsRecyclerView.addOnItemTouchListener(new GroupsRecyclerTouchListener(getActivity(),
                mGroupsRecyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                sendUserToGroupConversation(groups.get(position).getName(), groups.get(position).getAdminID());
            }

            @Override
            public void onLongClick(View child, int position) {

            }
        }));

        return groupFragmentView;
    }

    private void initView() {
        mGroupsRecyclerView = groupFragmentView.findViewById(R.id.groups_recycler_view);
    }

    private void setLayoutManager() {
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mGroupsRecyclerView.setLayoutManager(layoutManager);
    }

    private void getGroups() {
        groups = new ArrayList<>();
        db.collection("groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Group group = document.toObject(Group.class);

                                if (group.getUsers().contains(currentUser.getUid())) {
                                    groups.add(group);
                                }
                            }

                            setAdapter();
                        } else {
                            Log.d(MainActivity.TAG, "Error getting document: ", task.getException());
                            Toast.makeText(getContext(), "Failed getting groups", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setAdapter() {
        groupsAdapter = new GroupsAdapter(getContext(), groups);
        mGroupsRecyclerView.setAdapter(groupsAdapter);
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);
        builder.setTitle("Enter group name");

        final LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        final CircleImageView mGroupImage = new CircleImageView(getActivity());
        mGroupImage.setId(R.id.set_group_image);
        mGroupImage.setImageResource(R.drawable.group_image);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
        layout.addView(mGroupImage);

        final EditText mGroupName = new EditText(getActivity());
        mGroupName.setId(R.id.set_group_name);
        mGroupName.setGravity(Gravity.CENTER);
        mGroupName.setHint("Group name");
        layout.addView(mGroupName);

        builder.setView(layout);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText mGroupName = layout.findViewById(R.id.set_group_name);
                String groupName = mGroupName.getText().toString();

                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(getActivity(), "Please write the name of the group", Toast.LENGTH_SHORT).show();
                } else {
                    createNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog mCreateGroupDialog = builder.create();
        mCreateGroupDialog.show();
        mCreateGroupDialog.getWindow().setLayout(900, 1200);
    }

    private void createNewGroup(final String groupName) {
        Map<String, Object> groupData = new HashMap<String, Object>();
        groupData.put("name", groupName);
        groupData.put("adminID", currentUser.getUid());
        groupData.put("users", Arrays.asList(currentUser.getUid()));
        db.collection("groups")
                .document(groupName + "-" + currentUser.getUid())
                .set(groupData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Group created successfully", Toast.LENGTH_SHORT).show();
                        sendUserToGroupConversation(groupName, currentUser.getUid());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(MainActivity.TAG, "Error adding document", e);
                        Toast.makeText(getActivity(), "There was an error processing the request.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        getGroups();
    }

    private void sendUserToGroupConversation(String groupName, String adminID) {
        Intent groupChatIntent = new Intent(getActivity(), GroupChatActivity.class);
        groupChatIntent.putExtra("name", groupName);
        groupChatIntent.putExtra("adminID", adminID);
        startActivity(groupChatIntent);
    }
}

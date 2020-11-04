package com.example.snakemessenger.connectionRequests;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.database.Contact;
import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.database.Message;
import com.example.snakemessenger.R;
import com.example.snakemessenger.RecyclerTouchListener;
import com.example.snakemessenger.RecyclerViewClickListener;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConnectionRequestsFragment extends Fragment {
    private View connectionRequestsFragmentView;
    private TextView mNoConnectionRequests;
    private RecyclerView mConnectionRequestsRecyclerView;
    private ConnectionRequestsAdapter mAdapter;
    private List<ConnectionRequest> connectionRequests;

    private TextView mClosePopup;
    private CircleImageView mUserProfilePicture;
    private TextView mUserProfileName;
    private TextView mUserProfileStatus;
    private Button mAcceptRequest;
    private Button mDeleteRequest;

    private Dialog userProfileDialog;

    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                    Contact contact = MainActivity.db.getContactDao().findById(endpointId);
                    byte[] messageBytes = payload.asBytes();
                    String message = new String(messageBytes);

                    MainActivity.db.getMessageDao().addMessage(new Message(
                            0,
                            payload.getId(),
                            payload.getType(),
                            contact.getPhone(),
                            message,
                            Calendar.getInstance().getTime(),
                            Message.RECEIVED
                    ));

                    if (!contact.isChat()) {
                        contact.setChat(true);

                        MainActivity.db.getContactDao().updateContact(contact);
                    }

                    Log.d("ConnectionRequestsFrag", "onPayloadReceived: received a message from " + contact.getName());
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update) {
                    long payloadId = update.getPayloadId();

                    Message message = MainActivity.db.getMessageDao().getMessageByPayloadId(payloadId);

                    Log.d("ConnectionRequestsFrag", "onPayloadTransferUpdate: update about transfer with status " + update.getStatus());

                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS &&
                            message != null && message.getStatus() == Message.SENT) {
                        message.setStatus(Message.DELIVERED);
                        MainActivity.db.getMessageDao().updateMessage(message);

                        Log.d("ConnectionRequestsFrag", "onPayloadTransferUpdate: payload was delivered to its receiver");
                    }
                }
            };

    public ConnectionRequestsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        connectionRequestsFragmentView = inflater.inflate(R.layout.fragment_connection_requests, container, false);
        mNoConnectionRequests = connectionRequestsFragmentView.findViewById(R.id.no_connection_requests);
        mConnectionRequestsRecyclerView = connectionRequestsFragmentView.findViewById(R.id.friend_requests_recycler_view);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mConnectionRequestsRecyclerView.setLayoutManager(layoutManager);

        initializeUserProfileDialog();

        mConnectionRequestsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(),
                mConnectionRequestsRecyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                CircleImageView profilePic = view.findViewById(R.id.connection_request_image_item);
                BitmapDrawable drawable = (BitmapDrawable) profilePic.getDrawable();
                Bitmap image = drawable.getBitmap();

                ConnectionRequest connectionRequest = connectionRequests.get(position);

                showUserProfile(connectionRequest, image);
            }

            @Override
            public void onLongClick(View child, int position) {

            }
        }));

        return connectionRequestsFragmentView;
    }

    @Override
    public void onResume() {
        initializeConnectionRequestsList();
        super.onResume();
    }

    private void initializeConnectionRequestsList() {
        connectionRequests = MainActivity.connectionRequests;
        mAdapter = new ConnectionRequestsAdapter(getContext(), connectionRequests);
        mConnectionRequestsRecyclerView.setAdapter(mAdapter);

        if (connectionRequests.isEmpty()) {
            mNoConnectionRequests.setVisibility(View.VISIBLE);
        } else {
            mNoConnectionRequests.setVisibility(View.INVISIBLE);
        }
    }

    private void initializeUserProfileDialog() {
        userProfileDialog = new Dialog(getContext());
        userProfileDialog.setContentView(R.layout.user_profile_layout);

        mClosePopup = userProfileDialog.findViewById(R.id.user_profile_close);
        mUserProfilePicture = userProfileDialog.findViewById(R.id.user_profile_pic);
        mUserProfileName = userProfileDialog.findViewById(R.id.user_profile_name);
        mAcceptRequest = userProfileDialog.findViewById(R.id.accept_btn);
        mDeleteRequest = userProfileDialog.findViewById(R.id.decline_btn);

        mClosePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userProfileDialog.dismiss();
            }
        });
    }

    private void showUserProfile(ConnectionRequest request, Bitmap image) {
        final String userID = request.getSenderID();
        final String userName = request.getSenderName();

        mUserProfileName.setText(userName);
        mUserProfilePicture.setImageBitmap(image);

        mAcceptRequest.setText("Accept request");
        mAcceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Nearby.getConnectionsClient(getActivity())
                        .acceptConnection(userID, payloadCallback)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(
                                        getContext(),
                                        "You accepted " + userName + "'s connection request.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(
                                        getContext(),
                                        "There was an error accepting " + userName + "'s connection request.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });

                userProfileDialog.dismiss();
            }
        });

        mDeleteRequest.setText("Delete request");
        mDeleteRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Nearby.getConnectionsClient(getActivity())
                        .rejectConnection(userID)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(
                                        getContext(),
                                        "You rejected " + userName + "'s connection request.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(
                                        getContext(),
                                        "There was an error rejecting " + userName + "'s connection request.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });

                userProfileDialog.dismiss();
            }
        });

        userProfileDialog.show();
    }
}

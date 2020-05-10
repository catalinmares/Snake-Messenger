package com.example.snakemessenger;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendListManager {

    public static void processAddFriend(String currentUserID, String otherUserID) {
        Map<String, Object> friendRequestData = new HashMap<>();
        friendRequestData.put("sender", currentUserID);
        friendRequestData.put("receiver", otherUserID);
        friendRequestData.put("timestamp", Timestamp.now());

        FirebaseFirestore.getInstance()
                .collection("requests")
                .document()
                .set(friendRequestData);
    }

    public static void precessRemoveFriend(final String currentUserID, final String otherUserID) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(otherUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<String> friends = (List<String>) documentSnapshot.get("friends");
                            friends.remove(currentUserID);
                            documentSnapshot.getReference().update("friends", friends);
                        }
                    }
                });

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<String> friends = (List<String>) documentSnapshot.get("friends");
                            friends.remove(otherUserID);
                            documentSnapshot.getReference().update("friends", friends);
                        }
                    }
                });
    }

    public static void processAcceptFriendRequest(final String currentUserID, final String otherUserID) {
        FirebaseFirestore.getInstance()
                .collection("requests")
                .whereEqualTo("sender", otherUserID)
                .whereEqualTo("receiver", currentUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            doc.getReference().delete();
                        }
                    }
                });

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<String> friends = (List<String>) documentSnapshot.get("friends");
                            friends.add(otherUserID);
                            documentSnapshot.getReference().update("friends", friends);
                        }
                    }
                });

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(otherUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<String> friends = (List<String>) documentSnapshot.get("friends");
                            friends.add(currentUserID);
                            documentSnapshot.getReference().update("friends", friends);
                        }
                    }
                });
    }

    public static void processDeleteFriendRequest(String currentUserID, String otherUserID) {
        FirebaseFirestore.getInstance()
                .collection("requests")
                .whereEqualTo("sender", otherUserID)
                .whereEqualTo("receiver", currentUserID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                doc.getReference().delete();
                            }
                        }
                    }
                });
    }
}

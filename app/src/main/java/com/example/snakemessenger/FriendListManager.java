package com.example.snakemessenger;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FriendListManager {

    public static void processAddFriend(String currentUserID, String otherUserID) {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd/MM/yy");
        String currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm");
        String currentTime = currentTimeFormat.format(calForTime.getTime());

        Map<String, Object> friendRequestData = new HashMap<>();
        friendRequestData.put("userID", currentUserID);
        friendRequestData.put("time", currentTime);
        friendRequestData.put("date", currentDate);
        friendRequestData.put("timestamp", Timestamp.now());
        friendRequestData.put("status", "pending");

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(otherUserID)
                .collection("friend requests")
                .document(currentUserID)
                .set(friendRequestData);
    }

    public static void precessRemoveFriend(String currentUserID, String otherUserID) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(otherUserID)
                .collection("friends")
                .document(currentUserID)
                .delete();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserID)
                .collection("friends")
                .document(otherUserID)
                .delete();
    }

    public static void processAcceptFriendRequest(String currentUserID, String otherUserID) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserID)
                .collection("friend requests")
                .document(otherUserID)
                .update("status", "accepted");

        Map<String, Object> otherUserData = new HashMap<>();
        otherUserData.put("userID", otherUserID);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserID)
                .collection("friends")
                .document(otherUserID)
                .set(otherUserData);

        Map<String, Object> currentUserData = new HashMap<>();
        currentUserData.put("userID", currentUserID);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(otherUserID)
                .collection("friends")
                .document(currentUserID)
                .set(currentUserData);
    }

    public static void processDeleteFriendRequest(String currentUserID, String otherUserID) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserID)
                .collection("friend requests")
                .document(otherUserID)
                .update("status", "deleted");
    }
}

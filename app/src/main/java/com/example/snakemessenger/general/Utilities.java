package com.example.snakemessenger.general;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Log;

import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.crypto.CryptoManager;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.models.ImageMessage;
import com.example.snakemessenger.models.ImagePart;
import com.example.snakemessenger.models.Message;
import com.example.snakemessenger.notifications.NotificationHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import static com.example.snakemessenger.MainActivity.currentChat;
import static com.example.snakemessenger.MainActivity.db;
import static com.example.snakemessenger.MainActivity.myDeviceId;
import static com.example.snakemessenger.services.BackgroundCommunicationService.TAG;

public class Utilities {
    public static Message saveOwnMessageToDatabase(JSONObject messageJSON, long payloadId, int messageStatus) {
        try {
            String encryptionKey = messageJSON.getString(Constants.JSON_ENCRYPTION_KEY);
            String encryptedMessage = messageJSON.getString(Constants.JSON_MESSAGE_CONTENT_KEY);
            String decryptedMessage = CryptoManager.INSTANCE.decryptMessage(encryptionKey, encryptedMessage);

            Message message = new Message(
                    0,
                    messageJSON.getString(Constants.JSON_MESSAGE_ID_KEY),
                    payloadId,
                    messageJSON.getInt(Constants.JSON_MESSAGE_TYPE_KEY),
                    messageJSON.getString(Constants.JSON_SOURCE_DEVICE_ID_KEY),
                    messageJSON.getString(Constants.JSON_DESTINATION_DEVICE_ID_KEY),
                    messageJSON.getInt(Constants.JSON_CONTENT_TYPE_KEY),
                    decryptedMessage,
                    messageJSON.getLong(Constants.JSON_MESSAGE_TOTAL_SIZE),
                    messageJSON.getLong(Constants.JSON_MESSAGE_TIMESTAMP_KEY),
                    0,
                    messageStatus
            );

            db.getMessageDao().addMessage(message);

            Log.d(MainActivity.TAG, "saveOwnMessageToDatabase: saved Own Message to Room");

            Contact contact;

            if (messageStatus == Constants.MESSAGE_STATUS_SENT) {
                contact = db.getContactDao().findByDeviceId(messageJSON.getString(Constants.JSON_DESTINATION_DEVICE_ID_KEY));
            } else {
                contact = db.getContactDao().findByDeviceId(messageJSON.getString(Constants.JSON_SOURCE_DEVICE_ID_KEY));
            }

            contact.setLastMessageTimestamp(messageJSON.getLong(Constants.JSON_MESSAGE_TIMESTAMP_KEY));

            if (!contact.isChat()) {
                contact.setChat(true);

                Log.d(MainActivity.TAG, "saveOwnMessageToDatabase: user is a new chat contact");
            }

            db.getContactDao().updateContact(contact);

            return message;
        } catch (JSONException e) {
            Log.d(MainActivity.TAG, "saveOwnMessageToDatabase: could not save Own Message to Room. Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static void saveDataMemoryMessageToDatabase(JSONObject messageJSON, long payloadId, int messageStatus) {
        try {
            String encryptionKey = messageJSON.getString(Constants.JSON_ENCRYPTION_KEY);
            String encryptedMessage = messageJSON.getString(Constants.JSON_MESSAGE_CONTENT_KEY);
            String decryptedMessage = CryptoManager.INSTANCE.decryptMessage(encryptionKey, encryptedMessage);

            Message message = new Message(
                    0,
                    messageJSON.getString(Constants.JSON_MESSAGE_ID_KEY),
                    payloadId,
                    messageJSON.getInt(Constants.JSON_MESSAGE_TYPE_KEY),
                    messageJSON.getString(Constants.JSON_SOURCE_DEVICE_ID_KEY),
                    messageJSON.getString(Constants.JSON_DESTINATION_DEVICE_ID_KEY),
                    messageJSON.getInt(Constants.JSON_CONTENT_TYPE_KEY),
                    decryptedMessage,
                    messageJSON.getLong(Constants.JSON_MESSAGE_TOTAL_SIZE),
                    messageJSON.getLong(Constants.JSON_MESSAGE_TIMESTAMP_KEY),
                    0,
                    messageStatus
            );

            db.getMessageDao().addMessage(message);

            Log.d(MainActivity.TAG, "saveMessageInfoToDatabase: saved Data Memory Message to Room");
        } catch (JSONException e) {
            Log.d(MainActivity.TAG, "saveDataMemoryMessageToDatabase: could not save Data Memory Message to Room. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showImagePickDialog(Context context) {
        String[] options = {Constants.OPTION_CAMERA, Constants.OPTION_GALLERY};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(Constants.PICK_PICTURE_TEXT)
                .setItems(options, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            if (((Activity) context).checkSelfPermission(Manifest.permission.CAMERA) ==
                                    PackageManager.PERMISSION_DENIED) {
                                String[] permission = {Manifest.permission.CAMERA};

                                ((Activity) context).requestPermissions(permission, Constants.REQUEST_IMAGE_CAPTURE);
                            } else {
                                Utilities.dispatchTakePictureIntent(context);
                            }

                            break;

                        case 1:
                            if (((Activity) context).checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_DENIED) {
                                String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};

                                ((Activity) context).requestPermissions(permission, Constants.REQUEST_ACCESS_GALLERY);
                            } else {
                                Utilities.dispatchPickPictureIntent(context);
                            }
                    }
                })
                .show();
    }

    public static void dispatchTakePictureIntent(Context context) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            ((Activity) context).startActivityForResult(takePictureIntent, Constants.REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dispatchPickPictureIntent(Context context) {
        Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        try {
            ((Activity) context).startActivityForResult(pickPictureIntent, Constants.REQUEST_ACCESS_GALLERY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveImageToDatabase(Context context, Contact contact, ImageMessage imageMessage) {
        List<ImagePart> imageParts = imageMessage.getParts();
        Collections.sort(imageParts);

        ByteBuffer imageByteBuffer = ByteBuffer.allocate(imageMessage.getTotalSize());

        for (ImagePart part : imageParts) {
            Log.d(TAG, "saveImageToDatabase: adding chunk " + part.getPartNo());
            imageByteBuffer.put(part.getContent());
        }

        byte[] imageBytes = imageByteBuffer.array();
        Log.d(TAG, "saveImageToDatabase: image byte array has size " + imageBytes.length);
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        if (imageBitmap == null) {
            Log.d(TAG, "saveImageToDatabase: image bitmap is null!");
            return;
        }

        String imagePath = MediaStore.Images.Media.insertImage(context.getContentResolver(), imageBitmap, imageMessage.getMessageId(), null);

        JSONObject messageJSON = new JSONObject();

        try {
            String encryptionKey = CryptoManager.INSTANCE.generateKey();
            String encryptedMessage = CryptoManager.INSTANCE.encryptMessage(encryptionKey, imagePath);
            messageJSON.put(Constants.JSON_MESSAGE_ID_KEY, imageMessage.getMessageId());
            messageJSON.put(Constants.JSON_SOURCE_DEVICE_ID_KEY, imageMessage.getSourceId());
            messageJSON.put(Constants.JSON_DESTINATION_DEVICE_ID_KEY, imageMessage.getDestinationId());
            messageJSON.put(Constants.JSON_MESSAGE_TIMESTAMP_KEY, imageMessage.getTimestamp());
            messageJSON.put(Constants.JSON_CONTENT_TYPE_KEY, Constants.CONTENT_IMAGE);
            messageJSON.put(Constants.JSON_MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_MESSAGE);
            messageJSON.put(Constants.JSON_ENCRYPTION_KEY, encryptionKey);
            messageJSON.put(Constants.JSON_MESSAGE_CONTENT_KEY, encryptedMessage);
            messageJSON.put(Constants.JSON_MESSAGE_TOTAL_SIZE, imageMessage.getTotalSize());
        } catch (JSONException e) {
            Log.d(TAG, "saveImageToDatabase: could not save image. Error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        String destinationId = imageMessage.getDestinationId();

        if (destinationId.equals(myDeviceId)) {
            Log.d(TAG, "saveImageToDatabase: the message is for the current device");

            Message receivedMessage = saveOwnMessageToDatabase(messageJSON, imageMessage.getPayloadId(), Constants.MESSAGE_STATUS_RECEIVED);

            if (currentChat == null || !currentChat.equals(contact.getDeviceID())) {
                NotificationHandler.sendMessageNotification(context, contact, receivedMessage);
            }
        } else {
            Log.d(TAG, "saveImageToDatabase: the message is routing to another device");

            saveDataMemoryMessageToDatabase(messageJSON, imageMessage.getPayloadId(), Constants.MESSAGE_STATUS_ROUTING);
        }
    }
}

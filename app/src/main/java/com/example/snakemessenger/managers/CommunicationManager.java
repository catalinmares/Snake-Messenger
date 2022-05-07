package com.example.snakemessenger.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.snakemessenger.crypto.CryptoManager;
import com.example.snakemessenger.general.Utilities;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.models.Message;
import com.example.snakemessenger.general.Constants;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import android.util.Base64;
import java.util.List;
import java.util.UUID;

import static com.example.snakemessenger.MainActivity.TAG;
import static com.example.snakemessenger.MainActivity.myDeviceId;
import static com.example.snakemessenger.MainActivity.db;

public class CommunicationManager {
    public static void deliverMessage(Context context, Message message, Contact contact) {
        Log.d(TAG, "deliverMessage: delivering message with ID " + message.getMessageId() + " to " + contact.getName());

        if (message.getContentType() == Constants.CONTENT_IMAGE && message.getTotalSize() > Constants.MAX_IMAGE_SIZE) {
            Log.d(TAG, "deliverMessage: message contains an image that must be sent in chunks");

            String imagePath = message.getContent();
            long payloadId = 0;

            Uri imageUri = Uri.parse(imagePath);

            Bitmap imageBitmap = null;

            try {
                imageBitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (imageBitmap == null) {
                Log.d(TAG, "deliverMessage: image bitmap is null!");
                return;
            }

            int size = imageBitmap.getRowBytes() * imageBitmap.getHeight();

            Log.d(TAG, "buildAndDeliverImageMessage: image size is " + size + " B");

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
            byte[] imageBytes = stream.toByteArray();

            for (int i = 0, count = 0; i < imageBytes.length; i += Constants.MAX_IMAGE_SIZE, count++) {
                Log.d(TAG, "deliverMessage: sending chunk " + count + " of the image");

                byte[] imageChunk = (imageBytes.length - i - 1) > Constants.MAX_IMAGE_SIZE ?
                        new byte[Constants.MAX_IMAGE_SIZE] : new byte[imageBytes.length - i];
                int lastIdx = i;

                if ((imageBytes.length - i - 1) > Constants.MAX_IMAGE_SIZE) {
                    lastIdx += Constants.MAX_IMAGE_SIZE;
                } else {
                    lastIdx += imageBytes.length - i;
                }

                if (lastIdx - i >= 0) {
                    System.arraycopy(imageBytes, i, imageChunk, 0, lastIdx - i);
                }

                Log.d(TAG, "deliverMessage: chunk has size " + (lastIdx - i));

                String chunkContent = Base64.encodeToString(imageChunk, Base64.DEFAULT);

                JSONObject messageJSON = new JSONObject();

                try {
                    String encryptionKey = CryptoManager.INSTANCE.generateKey();
                    String encryptedMessage = CryptoManager.INSTANCE.encryptMessage(encryptionKey, chunkContent);

                    messageJSON.put(Constants.JSON_MESSAGE_ID_KEY, message.getMessageId());
                    messageJSON.put(Constants.JSON_SOURCE_DEVICE_ID_KEY, message.getSource());
                    messageJSON.put(Constants.JSON_DESTINATION_DEVICE_ID_KEY, message.getDestination());
                    messageJSON.put(Constants.JSON_MESSAGE_TIMESTAMP_KEY, message.getTimestamp());
                    messageJSON.put(Constants.JSON_CONTENT_TYPE_KEY, message.getContentType());
                    messageJSON.put(Constants.JSON_MESSAGE_TYPE_KEY, message.getType());
                    messageJSON.put(Constants.JSON_ENCRYPTION_KEY, encryptionKey);
                    messageJSON.put(Constants.JSON_MESSAGE_CONTENT_KEY, encryptedMessage);
                    messageJSON.put(Constants.JSON_IMAGE_PART_NO_KEY, count);
                    messageJSON.put(Constants.JSON_IMAGE_PART_SIZE_KEY, lastIdx - i);
                    messageJSON.put(Constants.JSON_IMAGE_SIZE_KEY, imageBytes.length);

                    InputStream messageStream = new ByteArrayInputStream(messageJSON.toString().getBytes());
                    Payload messagePayload = Payload.fromStream(messageStream);
                    Nearby.getConnectionsClient(context).sendPayload(contact.getEndpointID(), messagePayload);
                    payloadId = messagePayload.getId();
                    Log.d(TAG, "deliverMessage: sent chunk " + count + " of image having payload ID " + payloadId);
                } catch (JSONException e) {
                    Log.d(TAG, "deliverMessage: could not deliver message. Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            message.setPayloadId(payloadId);
            db.getMessageDao().updateMessage(message);

            return;
        }

        Log.d(TAG, "deliverMessage: message can be sent as a single payload");

        JSONObject messageJSON = new JSONObject();

        try {
            String encryptionKey = CryptoManager.INSTANCE.generateKey();
            String encryptedMessage = CryptoManager.INSTANCE.encryptMessage(encryptionKey, message.getContent());

            messageJSON.put(Constants.JSON_MESSAGE_ID_KEY, message.getMessageId());
            messageJSON.put(Constants.JSON_SOURCE_DEVICE_ID_KEY, message.getSource());
            messageJSON.put(Constants.JSON_DESTINATION_DEVICE_ID_KEY, message.getDestination());
            messageJSON.put(Constants.JSON_MESSAGE_TIMESTAMP_KEY, message.getTimestamp());
            messageJSON.put(Constants.JSON_CONTENT_TYPE_KEY, message.getContentType());
            messageJSON.put(Constants.JSON_MESSAGE_TYPE_KEY, message.getType());
            messageJSON.put(Constants.JSON_ENCRYPTION_KEY, encryptionKey);
            messageJSON.put(Constants.JSON_MESSAGE_CONTENT_KEY, encryptedMessage);
            messageJSON.put(Constants.JSON_MESSAGE_TOTAL_SIZE, message.getContent().length());

            Payload messagePayload = Payload.fromBytes(messageJSON.toString().getBytes());
            message.setPayloadId(messagePayload.getId());
            Nearby.getConnectionsClient(context).sendPayload(contact.getEndpointID(), messagePayload);

            db.getMessageDao().updateMessage(message);
        } catch (JSONException e) {
            Log.d(TAG, "deliverMessage: could not deliver message. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Message buildAndDeliverMessage(Context context, String messageContent, Contact contact) {
        JSONObject messageJSON = new JSONObject();

        try {
            String encryptionKey = CryptoManager.INSTANCE.generateKey();
            String encryptedMessage = CryptoManager.INSTANCE.encryptMessage(encryptionKey, messageContent);

            messageJSON.put(Constants.JSON_MESSAGE_ID_KEY, UUID.randomUUID().toString());
            messageJSON.put(Constants.JSON_SOURCE_DEVICE_ID_KEY, myDeviceId);
            messageJSON.put(Constants.JSON_DESTINATION_DEVICE_ID_KEY, contact.getDeviceID());
            messageJSON.put(Constants.JSON_MESSAGE_TIMESTAMP_KEY, System.currentTimeMillis());
            messageJSON.put(Constants.JSON_CONTENT_TYPE_KEY, Constants.CONTENT_TEXT);
            messageJSON.put(Constants.JSON_MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_MESSAGE);
            messageJSON.put(Constants.JSON_MESSAGE_CONTENT_KEY, encryptedMessage);
            messageJSON.put(Constants.JSON_ENCRYPTION_KEY, encryptionKey);
            messageJSON.put(Constants.JSON_MESSAGE_TOTAL_SIZE, messageContent.length());

            Payload messagePayload = Payload.fromBytes(messageJSON.toString().getBytes());
            Nearby.getConnectionsClient(context).sendPayload(contact.getEndpointID(), messagePayload);

            return Utilities.saveOwnMessageToDatabase(messageJSON, messagePayload.getId(), Constants.MESSAGE_STATUS_SENT);
        } catch (JSONException e) {
            Log.d(TAG, "buildAndDeliverMessage: could not deliver message. Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static void buildAndDeliverImageMessage(Context context, Bitmap imageBitmap, String imagePath, Contact contact) {
        Log.d(TAG, "buildAndDeliverImageMessage: building and sending image message to " + contact.getName());

        if (imageBitmap == null) {
            Log.d(TAG, "buildAndDeliverImageMessage: image bitmap is null!");
            return;
        }

        int size = imageBitmap.getRowBytes() * imageBitmap.getHeight();

        Log.d(TAG, "buildAndDeliverImageMessage: image size is " + size + " B");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        byte[] imageBytes = stream.toByteArray();

        String messageId = UUID.randomUUID().toString();
        long payloadId = 0;
        long timestamp = System.currentTimeMillis();

        if (imagePath == null) {
            imagePath = MediaStore.Images.Media.insertImage(context.getContentResolver(), imageBitmap, messageId, null);
        }

        for (int i = 0, count = 0; i < imageBytes.length; i += Constants.MAX_IMAGE_SIZE, count++) {
            Log.d(TAG, "buildAndDeliverImageMessage: sending chunk " + count + " of the image");

            byte[] imageChunk = (imageBytes.length - i - 1) > Constants.MAX_IMAGE_SIZE ?
                    new byte[Constants.MAX_IMAGE_SIZE] : new byte[imageBytes.length - i];
            int lastIdx = i;

            if ((imageBytes.length - i - 1) > Constants.MAX_IMAGE_SIZE) {
                lastIdx += Constants.MAX_IMAGE_SIZE;
            } else {
                lastIdx += imageBytes.length - i;
            }

            if (lastIdx - i >= 0) {
                System.arraycopy(imageBytes, i, imageChunk, 0, lastIdx - i);
            }

            Log.d(TAG, "buildAndDeliverImageMessage: chunk has size " + (lastIdx - i));

            String chunkContent = Base64.encodeToString(imageChunk, Base64.DEFAULT);

            JSONObject messageJSON = new JSONObject();

            try {
                String encryptionKey = CryptoManager.INSTANCE.generateKey();
                String encryptedMessage = CryptoManager.INSTANCE.encryptMessage(encryptionKey, chunkContent);

                messageJSON.put(Constants.JSON_MESSAGE_ID_KEY, messageId);
                messageJSON.put(Constants.JSON_SOURCE_DEVICE_ID_KEY, myDeviceId);
                messageJSON.put(Constants.JSON_DESTINATION_DEVICE_ID_KEY, contact.getDeviceID());
                messageJSON.put(Constants.JSON_MESSAGE_TIMESTAMP_KEY, timestamp);
                messageJSON.put(Constants.JSON_CONTENT_TYPE_KEY, Constants.CONTENT_IMAGE);
                messageJSON.put(Constants.JSON_MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_MESSAGE);
                messageJSON.put(Constants.JSON_ENCRYPTION_KEY, encryptionKey);
                messageJSON.put(Constants.JSON_MESSAGE_CONTENT_KEY, encryptedMessage);
                messageJSON.put(Constants.JSON_IMAGE_PART_NO_KEY, count);
                messageJSON.put(Constants.JSON_IMAGE_PART_SIZE_KEY, lastIdx - i);
                messageJSON.put(Constants.JSON_IMAGE_SIZE_KEY, imageBytes.length);

                InputStream messageStream = new ByteArrayInputStream(messageJSON.toString().getBytes());
                Payload messagePayload = Payload.fromStream(messageStream);
                Nearby.getConnectionsClient(context).sendPayload(contact.getEndpointID(), messagePayload);
                payloadId = messagePayload.getId();

                Log.d(TAG, "buildAndDeliverImageMessage: sent image chunk with payload ID " + payloadId);
            } catch (JSONException e) {
                Log.d(TAG, "buildAndDeliverImageMessage: could not deliver message. Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        JSONObject messageJSON = new JSONObject();

        try {
            String encryptionKey = CryptoManager.INSTANCE.generateKey();
            String encryptedMessage = CryptoManager.INSTANCE.encryptMessage(encryptionKey, imagePath);

            messageJSON.put(Constants.JSON_MESSAGE_ID_KEY, messageId);
            messageJSON.put(Constants.JSON_SOURCE_DEVICE_ID_KEY, myDeviceId);
            messageJSON.put(Constants.JSON_DESTINATION_DEVICE_ID_KEY, contact.getDeviceID());
            messageJSON.put(Constants.JSON_MESSAGE_TIMESTAMP_KEY, timestamp);
            messageJSON.put(Constants.JSON_CONTENT_TYPE_KEY, Constants.CONTENT_IMAGE);
            messageJSON.put(Constants.JSON_MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_MESSAGE);
            messageJSON.put(Constants.JSON_ENCRYPTION_KEY, encryptionKey);
            messageJSON.put(Constants.JSON_MESSAGE_CONTENT_KEY, encryptedMessage);
            messageJSON.put(Constants.JSON_MESSAGE_TOTAL_SIZE, imageBytes.length);

            Utilities.saveOwnMessageToDatabase(messageJSON, payloadId, Constants.MESSAGE_STATUS_SENT);
        } catch (JSONException e) {
            Log.d(TAG, "buildAndDeliverImageMessage: could not deliver image. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void deliverDirectMessages(Context context, Contact contact) {
        Log.d(TAG, "deliverDirectMessages: delivering direct messages to " + contact.getName());

        List<Message> undeliveredMessages = db.getMessageDao().getUndeliveredMessages(contact.getDeviceID());

        Log.d(TAG, "deliverDirectMessages: there are " + undeliveredMessages.size() + " messages to deliver.");

        for (Message message : undeliveredMessages) {
            deliverMessage(context, message, contact);
        }
    }

    public static void sendDeviceInformation(Context context, Contact contact, float batteryLevel) {
        Log.d(TAG, "sendDeviceInformation: sending device information to " + contact.getName());

        List<Contact> lastInteractionsContacts = db.getContactDao().getLastInteractions();
        JSONArray lastInteractions = new JSONArray();

        for (Contact interactionContact : lastInteractionsContacts) {
           JSONObject contactJSON = new JSONObject();
            try {
                contactJSON.put(Constants.JSON_DEVICE_ID_KEY, interactionContact.getDeviceID());

                if (interactionContact.isConnected()) {
                    contactJSON.put(Constants.JSON_DEVICE_LAST_CONTACT_KEY, System.currentTimeMillis());
                } else {
                    contactJSON.put(Constants.JSON_DEVICE_LAST_CONTACT_KEY, interactionContact.getLastActive());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

           lastInteractions.put(contactJSON);
        }

        JSONObject messageJSON = new JSONObject();

        try {
            messageJSON.put(Constants.JSON_MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_HELLO);
            messageJSON.put(Constants.JSON_BATTERY_KEY, batteryLevel);
            messageJSON.put(Constants.JSON_CONTACTS_KEY, lastInteractions);

            Payload messagePayload = Payload.fromBytes(messageJSON.toString().getBytes());
            Nearby.getConnectionsClient(context).sendPayload(contact.getEndpointID(), messagePayload);
        } catch (JSONException e) {
            Log.d(TAG, "sendDeviceInformation: could not deliver message. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void deliverACKMessage(Context context, Contact contact) {
        Log.d(TAG, "deliverACKMessage: delivering ACK message to " + contact.getName());

        List<Message> lastReceivedMessages = db.getMessageDao().getLastReceivedMessages();
        Log.d(TAG, "deliverACKMessage: there are " + lastReceivedMessages.size() + " ACKs to deliver");

        if (lastReceivedMessages.isEmpty()) {
            return;
        }

        JSONArray messageACKs = new JSONArray();

        for (Message message : lastReceivedMessages) {
            JSONObject messageACK = new JSONObject();
            try {
                messageACK.put(Constants.JSON_MESSAGE_ID_KEY, message.getMessageId());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            messageACKs.put(messageACK);
        }

        JSONObject messageJSON = new JSONObject();

        try {
            messageJSON.put(Constants.JSON_MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_ACK);
            messageJSON.put(Constants.JSON_MESSAGES_KEY, messageACKs);

            Payload messagePayload = Payload.fromBytes(messageJSON.toString().getBytes());
            Nearby.getConnectionsClient(context).sendPayload(contact.getEndpointID(), messagePayload);
        } catch (JSONException e) {
            Log.d(TAG, "deliverACKMessage: could not deliver message. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendMessagesForRouting(Context context, JSONObject deviceInfo, Contact contact) {
        Log.d(TAG, "sendMessagesForRouting: trying to deliver undelivered messages to " + contact.getName());

        try {
            double batteryLevel = deviceInfo.getDouble(Constants.JSON_BATTERY_KEY);

            if (batteryLevel >= Constants.BATTERY_THRESHOLD) {
                Log.d(TAG, "sendMessagesForRouting: contact " + contact.getName() + "'s battery level is suitable to receive messages");

                JSONArray recentContacts = deviceInfo.getJSONArray(Constants.JSON_CONTACTS_KEY);

                for (int i = 0; i < recentContacts.length(); i++) {
                    JSONObject contactJSON = recentContacts.getJSONObject(i);

                    String contactDeviceId = contactJSON.getString(Constants.JSON_DEVICE_ID_KEY);

                    if (contactDeviceId.equals(myDeviceId)) {
                        continue;
                    }

                    long timestamp = contactJSON.getLong(Constants.JSON_DEVICE_LAST_CONTACT_KEY);
                    long now = System.currentTimeMillis();

                    if (now - timestamp > Constants.MILLIS_IN_DAY) {
                        Log.d(TAG, "sendMessagesForRouting: contact " + contact.getName() + " had contact with " + contactDeviceId + " more than 24 hours ago. Skipping messages...");
                        continue;
                    }

                    Log.d(TAG, "sendMessagesForRouting: sending messages for contact with device ID " + contactDeviceId);

                    List<Message> ownMessagesForContact = db.getMessageDao().getOwnMessages(myDeviceId, contactDeviceId);
                    Log.d(TAG, "sendMessagesForRouting: sending " + ownMessagesForContact.size() + " own messages");

                    for (Message message : ownMessagesForContact) {
                        deliverMessage(context, message, contact);
                    }

                    List<Message> dataMemoryForContact = db.getMessageDao().getDataMemory(myDeviceId, contactDeviceId);
                    Log.d(TAG, "sendMessagesForRouting: sending " + dataMemoryForContact.size() + " data memory messages.");

                    for (Message message : dataMemoryForContact) {
                        deliverMessage(context, message, contact);
                    }
                }

                deliverACKMessage(context, contact);
            } else {
                Log.d(TAG, "sendMessagesForRouting: contact " + contact.getName() + "'s battery is too low to receive other messages than his. Skipping transmission....");
            }
        } catch (JSONException e) {
            Log.d(TAG, "sendMessagesForRouting: could not send messages for routing to " + contact.getName() + ". Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void markMessagesAsDelivered(JSONObject messageJSON) {
        try {
            JSONArray messagesArray = messageJSON.getJSONArray(Constants.JSON_MESSAGES_KEY);

            for (int i = 0; i < messagesArray.length(); i++) {
                JSONObject message = messagesArray.getJSONObject(i);
                String messageId = message.getString(Constants.JSON_MESSAGE_ID_KEY);

                Log.d(TAG, "markMessagesAsDelivered: received ACK for message with ID " + messageId);

                Message dbMessage = db.getMessageDao().findByMessageId(messageId);

                if (dbMessage != null) {
                    Log.d(TAG, "markMessagesAsDelivered: the message was found in the local database.");

                    if (dbMessage.getStatus() == Constants.MESSAGE_STATUS_SENT) {
                        Log.d(TAG, "markMessagesAsDelivered: the message was sent by the current device. Marking it as delivered...");

                        dbMessage.setStatus(Constants.MESSAGE_STATUS_DELIVERED);
                        db.getMessageDao().updateMessage(dbMessage);
                    } else if (dbMessage.getStatus() == Constants.MESSAGE_STATUS_ROUTING) {
                        Log.d(TAG, "markMessagesAsDelivered: the message is carried by the current device, but it has another source. Deleting it...");

                        db.getMessageDao().deleteMessage(dbMessage);
                    }
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "markMessagesAsDelivered: could not mark messages as delivered. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

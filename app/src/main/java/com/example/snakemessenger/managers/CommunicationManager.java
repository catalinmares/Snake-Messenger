package com.example.snakemessenger.managers;

import android.content.Context;
import android.util.Log;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.models.Message;
import com.example.snakemessenger.general.Constants;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

import static com.example.snakemessenger.MainActivity.TAG;
import static com.example.snakemessenger.MainActivity.myDeviceId;
import static com.example.snakemessenger.MainActivity.db;

public class CommunicationManager {
    public static void deliverMessage(Context context, Message message, Contact contact) {
        Log.d(TAG, "deliverMessage: delivering message with ID " + message.getMessageId() + " to " + contact.getName());

        JSONObject messageJSON = new JSONObject();

        try {
            messageJSON.put(Constants.JSON_MESSAGE_ID_KEY, message.getMessageId());
            messageJSON.put(Constants.JSON_SOURCE_DEVICE_ID_KEY, message.getSource());
            messageJSON.put(Constants.JSON_DESTINATION_DEVICE_ID_KEY, message.getDestination());
            messageJSON.put(Constants.JSON_MESSAGE_TIMESTAMP_KEY, message.getTimestamp());
            messageJSON.put(Constants.JSON_MESSAGE_TYPE_KEY, message.getType());
            messageJSON.put(Constants.JSON_MESSAGE_CONTENT_KEY, message.getContent());

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
            messageJSON.put(Constants.JSON_MESSAGE_ID_KEY, UUID.randomUUID().toString());
            messageJSON.put(Constants.JSON_SOURCE_DEVICE_ID_KEY, myDeviceId);
            messageJSON.put(Constants.JSON_DESTINATION_DEVICE_ID_KEY, contact.getDeviceID());
            messageJSON.put(Constants.JSON_MESSAGE_TIMESTAMP_KEY, System.currentTimeMillis());
            messageJSON.put(Constants.JSON_MESSAGE_TYPE_KEY, Constants.MESSAGE_TYPE_MESSAGE);
            messageJSON.put(Constants.JSON_MESSAGE_CONTENT_KEY, messageContent);

            Payload messagePayload = Payload.fromBytes(messageJSON.toString().getBytes());
            Nearby.getConnectionsClient(context).sendPayload(contact.getEndpointID(), messagePayload);

            return saveOwnMessageToDatabase(messageJSON, contact, messagePayload, Constants.MESSAGE_STATUS_SENT);
        } catch (JSONException e) {
            Log.d(TAG, "buildAndDeliverMessage: could not deliver message. Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
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

                    List<Message> dataMemoryForContact = db.getMessageDao().getDataMemory(myDeviceId, contactDeviceId);
                    Log.d(TAG, "sendMessagesForRouting: sending " + dataMemoryForContact.size() + " data memory messages.");

                    for (Message message : dataMemoryForContact) {
                        deliverMessage(context, message, contact);
                    }

                    List<Message> ownMessagesForContact = db.getMessageDao().getOwnMessages(myDeviceId, contactDeviceId);
                    Log.d(TAG, "sendMessagesForRouting: sending " + ownMessagesForContact.size() + " own messages");

                    for (Message message : ownMessagesForContact) {
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

    public static Message saveOwnMessageToDatabase(JSONObject messageJSON, Contact contact, Payload payload, int messageStatus) {
        try {
            Message message = new Message(
                    0,
                    messageJSON.getString(Constants.JSON_MESSAGE_ID_KEY),
                    payload.getId(),
                    messageJSON.getInt(Constants.JSON_MESSAGE_TYPE_KEY),
                    messageJSON.getString(Constants.JSON_SOURCE_DEVICE_ID_KEY),
                    messageJSON.getString(Constants.JSON_DESTINATION_DEVICE_ID_KEY),
                    messageJSON.getString(Constants.JSON_MESSAGE_CONTENT_KEY),
                    messageJSON.getLong(Constants.JSON_MESSAGE_TIMESTAMP_KEY),
                    0,
                    messageStatus
            );

            db.getMessageDao().addMessage(message);

            Log.d(TAG, "saveOwnMessageToDatabase: saved Own Message to Room");

            contact.setLastMessageTimestamp(messageJSON.getLong(Constants.JSON_MESSAGE_TIMESTAMP_KEY));

            if (!contact.isChat()) {
                contact.setChat(true);

                Log.d(TAG, "saveOwnMessageToDatabase: user is a new chat contact");
            }

            db.getContactDao().updateContact(contact);

            return message;
        } catch (JSONException e) {
            Log.d(TAG, "saveOwnMessageToDatabase: could not save Own Message to Room. Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static void saveDataMemoryMessageToDatabase(JSONObject messageJSON, Payload payload, int messageStatus) {
        try {
            Message message = new Message(
                    0,
                    messageJSON.getString(Constants.JSON_MESSAGE_ID_KEY),
                    payload.getId(),
                    messageJSON.getInt(Constants.JSON_MESSAGE_TYPE_KEY),
                    messageJSON.getString(Constants.JSON_SOURCE_DEVICE_ID_KEY),
                    messageJSON.getString(Constants.JSON_DESTINATION_DEVICE_ID_KEY),
                    messageJSON.getString(Constants.JSON_MESSAGE_CONTENT_KEY),
                    messageJSON.getLong(Constants.JSON_MESSAGE_TIMESTAMP_KEY),
                    0,
                    messageStatus
            );

            db.getMessageDao().addMessage(message);

            Log.d(TAG, "saveMessageInfoToDatabase: saved Data Memory Message to Room");
        } catch (JSONException e) {
            Log.d(TAG, "saveDataMemoryMessageToDatabase: could not save Data Memory Message to Room. Error: " + e.getMessage());
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

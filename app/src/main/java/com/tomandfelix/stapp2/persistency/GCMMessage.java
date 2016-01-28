package com.tomandfelix.stapp2.persistency;

/**
 * Created by Tom on 19/03/2015.
 */

public class GCMMessage {
    public enum MessageType {
        TEST_TOAST, REQUEST, ACCEPT, DECLINE, STOP, COMMUNICATION, RESULT
    }

    private int[] receivers;
    private String uniqueId;
    private MessageType messageType;
    private int senderId;
    private String message;

    public GCMMessage(int[] receivers, String uniqueId, MessageType messageType, int senderId, String message) {
        this.receivers = receivers;
        this.uniqueId = uniqueId;
        this.messageType = messageType;
        this.senderId = senderId;
        this.message = message;
    }

    public int[] getReceivers() {
        return receivers;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        String result = "uniqueId: " + uniqueId;
        result += " messageType: " + messageType;
        result += " message: '" + message + "'";
        result += " sender: " + senderId;
        result += " receiver_ids: ";
        for(int i = 0; i < receivers.length - 1; i++) {
            result += receivers[i] + ", ";
        }
        result += receivers[receivers.length - 1];
        return result;
    }
}

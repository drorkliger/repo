package bgu.spl.net.api.messages;

import bgu.spl.net.api.Message;

public class NotificationMessage implements Message {

    private short opCode;
    private byte NT;//NOTIFICATION TYPE
    private String PostingUser;
    private String content;
    private int senderConnectionId;

    public NotificationMessage(byte b, String postingUser, String content) {
        this.PostingUser=postingUser;
        this.content=content;
        opCode=9;
        NT=b;
    }

    public short getOpCode() {
        return opCode;
    }

    public byte getNT() {
        return NT;
    }

    public String getContent() {
        return content;
    }

    public String getPostingUser() {
        return PostingUser;
    }
}
package bgu.spl.net.api.messages;

import bgu.spl.net.api.Message;

public class PMMessage implements Message {
    private String Username;
    private String Content;
    private int numOfZeros;
    private short opCode;


    public PMMessage() {
        opCode=6;
        numOfZeros=2;
        Username=null;
        Content=null;
    }


    public int getNumOfZeros() {
        return numOfZeros;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    @Override
    public short getOpCode() {
        return opCode;
    }
}

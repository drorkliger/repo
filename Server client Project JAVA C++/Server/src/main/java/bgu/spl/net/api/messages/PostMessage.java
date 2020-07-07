package bgu.spl.net.api.messages;

import bgu.spl.net.api.Message;

public class PostMessage implements Message {

    private int numOfZeros;
    private String content;
    private short opCode;

    public PostMessage()
    {
        opCode=5;
        numOfZeros=1;
        content=null;
    }

    public int getNumOfZeros() {
        return numOfZeros;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public short getOpCode() {
        return opCode;
    }
}

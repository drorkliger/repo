package bgu.spl.net.api.messages;

import bgu.spl.net.api.Message;

public class StatMessage implements Message {

    private int numOfZeros;
    private String UserName;
    private short opCode;


    public StatMessage()
    {
        opCode=8;
        numOfZeros=1;
        UserName=null;

    }

    public int getNumOfZeros() {
        return numOfZeros;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    @Override
    public short getOpCode() {
        return opCode;
    }

}

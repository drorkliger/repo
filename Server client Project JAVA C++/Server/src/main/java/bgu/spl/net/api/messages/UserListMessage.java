package bgu.spl.net.api.messages;

import bgu.spl.net.api.Message;

public class UserListMessage implements Message {
    private int numOfZeros;
    private short opCode;

    public UserListMessage()
    {
        opCode=7;
        numOfZeros=0;
    }

    public int getNumOfZeros() {
        return numOfZeros;
    }

    @Override
    public short getOpCode() {
        return opCode;
    }
}

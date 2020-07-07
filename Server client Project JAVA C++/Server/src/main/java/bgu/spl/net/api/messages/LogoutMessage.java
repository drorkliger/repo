package bgu.spl.net.api.messages;

import bgu.spl.net.api.Message;

public class LogoutMessage implements Message {

    private int numOfZeros;
    private short opCode;

    public LogoutMessage()
    {
        opCode=3;
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

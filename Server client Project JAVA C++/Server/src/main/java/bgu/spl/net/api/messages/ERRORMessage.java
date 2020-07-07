package bgu.spl.net.api.messages;

import bgu.spl.net.api.Message;

public class ERRORMessage implements Message {

    private short opCode;
    private short msgOpCode;//the opcode of the message that cause the error

    public ERRORMessage(short msgOpCode) {
        this.msgOpCode=msgOpCode;
        opCode=11;
    }

    public short getOpCode() {
        return opCode;
    }

    public short getMsgOpCode() {
        return msgOpCode;
    }
}

package bgu.spl.net.api.messages;

import bgu.spl.net.api.Message;

public class LoginMessage implements Message {

    private String Username;
    private String Password;
    private int numOfZeros;
    private short opCode;

    public LoginMessage() {
        numOfZeros=2;
        opCode=2;
        Username=null;
        Password=null;
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

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    @Override
    public short getOpCode() {
        return opCode;
    }
}

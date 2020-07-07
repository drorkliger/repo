package bgu.spl.net.api.messages;

import bgu.spl.net.api.Message;

import java.util.LinkedList;
import java.util.List;

public class ACKMessage implements Message {

    private short opCode;



    private short msgOpCode;//the opcode of the message that cause the ack
    private short NumPosts;
    private short NumFollowers;
    private short NumFollowing;
    private short NumOfUsers;
    private List<String> UserNameList;

    public ACKMessage(short msgOpCode) {
        UserNameList=new LinkedList<>();
        NumOfUsers=0;
        NumFollowers=0;
        NumFollowing=0;
        NumPosts=0;
        this.msgOpCode=msgOpCode;
        opCode=10;
    }

    public short getOpCode() {
        return opCode;
    }

    public short getMsgOpCode() {
        return msgOpCode;
    }

    public short getNumPosts() {
        return NumPosts;
    }

    public short getNumFollowers() {
        return NumFollowers;
    }

    public short getNumFollowing() {
        return NumFollowing;
    }

    public short getNumOfUsers() {
        return NumOfUsers;
    }

    public List<String> getUserNameList() {
        return UserNameList;
    }

    public void setUserNameList(List<String> userNameList) {
        UserNameList = userNameList;
    }

    public void setNumOfUsers(short numOfUsers) {
        NumOfUsers = numOfUsers;
    }

    public void setNumPosts(short numPosts) {
        NumPosts = numPosts;
    }

    public void setNumFollowers(short numFollowers) {
        NumFollowers = numFollowers;
    }

    public void setNumFollowing(short numFollowing) {
        NumFollowing = numFollowing;
    }
}

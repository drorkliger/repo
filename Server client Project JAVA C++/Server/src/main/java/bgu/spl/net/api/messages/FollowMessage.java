package bgu.spl.net.api.messages;

import bgu.spl.net.api.Message;

import java.util.LinkedList;
import java.util.List;

public class FollowMessage implements Message
{
    enum Action
    {FOLLOW,UNFOLLOW;}

    private Action toFollow;
    private List<String> users;
    private short opCode;


    public FollowMessage()
    {
        opCode=4;
        users=new LinkedList<>();
        toFollow=null;
    }

    public int getToFollow() {
        if(toFollow==Action.FOLLOW)
            return 0;
        return 1;
    }

    public void setToFollow(int follow) {
        if(follow==0)
            this.toFollow=Action.FOLLOW;
        if(follow==1)
            this.toFollow=Action.UNFOLLOW;
    }

    public void addUsers(String [] usersList)
    {
        for(String user:usersList)
            users.add(user);
    }

    public void addUser(String userName)
    {
        users.add(userName);
    }

    @Override
    public short getOpCode() {
        return opCode;
    }

    public List<String> getUsers() {
        return users;
    }

}

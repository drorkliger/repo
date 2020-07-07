package bgu.spl.net.api;

import bgu.spl.net.api.messages.NotificationMessage;
import bgu.spl.net.api.messages.PMMessage;
import bgu.spl.net.api.messages.PostMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class User {

    private String userName;
    private String password;
    private int connectionId;
    private LinkedBlockingQueue<NotificationMessage> messagesNotSent;
    private LinkedBlockingQueue <String> following;//users that this user follows
    private LinkedBlockingQueue <String> followers;//users that this user is being followed by
    private boolean isLogged;
    private LinkedBlockingQueue<PostMessage>posts;
    private LinkedBlockingQueue<PMMessage>privateMessages;
    private Object userLock;


    public User(String userName, String password) {
        this.userName=userName;
        this.password=password;
        this.isLogged=false;
        this.connectionId=-1;
        this.followers=new LinkedBlockingQueue<>();
        this.following=new LinkedBlockingQueue<>();
        this.posts=new LinkedBlockingQueue<>();
        this.messagesNotSent=new LinkedBlockingQueue<>();
        this.privateMessages=new LinkedBlockingQueue<>();
        this.userLock=new Object();
    }


    public String getUserName() {
        return userName;
    }


    public String getPassword() {
        return password;
    }


    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public boolean isLogged() {return isLogged;}

    public void setLogged(boolean logged) {isLogged = logged;}

    public LinkedBlockingQueue<String> getFollowing() {
        return following;
    }

    public LinkedBlockingQueue<String> getFollowers() {
        return followers;
    }

    public LinkedBlockingQueue<NotificationMessage> getNotificationQueue(){
        return messagesNotSent;
    }

    public NotificationMessage getNotificationMessage() {
        return messagesNotSent.poll();
    }


    public void addPost(PostMessage msg)
    {
        try {
            posts.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addPM(PMMessage msg)
    {
        try {
            privateMessages.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public LinkedBlockingQueue<PostMessage> getPosts() {
        return posts;
    }

    public Object getUserLock() {
        return userLock;
    }


    //-----------------getters & setters-----------------------------------------------------


    public void addNotificationMessage(NotificationMessage notificationMessage)
    {
        messagesNotSent.add(notificationMessage);
    }

    public void followUser(String userName)
    {//users that this user follows

        try {
        following.put(userName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addFollowingUser(String userName){//users that this user is being followed by
        try {
            followers.put(userName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

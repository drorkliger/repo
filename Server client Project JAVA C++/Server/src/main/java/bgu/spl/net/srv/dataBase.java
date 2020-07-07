package bgu.spl.net.srv;

import bgu.spl.net.api.Message;
import bgu.spl.net.api.User;
import bgu.spl.net.api.messages.NotificationMessage;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class dataBase {

    private ConcurrentHashMap<Integer, String> connectionIdUserName;//key - connection id, value - username that logged in this connection handler
    private ConcurrentHashMap<String, User> RegisteredUsers;// key- userName, value - User object
    private List <String> registeredByOrder;
    private Object registerLock;
    private Object loginLock;


    public dataBase() {
        this.RegisteredUsers=new ConcurrentHashMap<>();
        this.connectionIdUserName=new ConcurrentHashMap<>();
        this.registerLock=new Object();
        this.loginLock=new Object();
        this.registeredByOrder=new LinkedList();

    }


    public User getUserByUserName(String userName)
    {
        return RegisteredUsers.get(userName);
    }

    public void addUnregisteredUser (String username, String password)
    {
        RegisteredUsers.putIfAbsent(username,new User(username,password));
    }

    public void setConnectionIdUserName(int connectionId, String userName)//
    {
        connectionIdUserName.putIfAbsent(connectionId, userName);
    }

    public void setByOrder (String username)
    {
        registeredByOrder.add(username);
    }

    public List<String> getRegisteredByOrder() {
        return registeredByOrder;
    }

    public String getUsernameOfConnectionId(Integer id) {
        return connectionIdUserName.get(id);
    }


    public void removeConnectionId(int connectioId) {
        connectionIdUserName.remove(connectioId);
    }

    public List<String> getAllUsers()//returns a list of all the user names
    {
        List<String> allUsers=new LinkedList<>();
        for(String userName: RegisteredUsers.keySet())
        {
            allUsers.add(userName);
        }
        return allUsers;
    }


    public Object getRegisterLock() {
        return registerLock;
    }

    public Object getLoginLock() {
        return loginLock;
    }

    public List<String> addFollowUsers(String followingUser, List<String> users) {
        List<String> successfulFollow = new LinkedList<>();
        User followingUserObj=RegisteredUsers.get(followingUser);
        for (String user : users) {
            if ((followingUserObj != null) && (!followingUserObj.getFollowing().contains(user)) && (this.getUserByUserName(user)!=null))//if he doesnt follow the user and the user exists
            {
                try {
                    RegisteredUsers.get(followingUser).getFollowing().put(user);//add to the follower list
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    RegisteredUsers.get(user).getFollowers().put(followingUser);//add to the followed list
                     } catch (InterruptedException e) {
                    e.printStackTrace();
                }




                successfulFollow.add(user);
            }
        }
        return successfulFollow;
    }

    public List<String> removeUnFollowUsers(String followingUser, List<String> users) {
        List<String> successfulUnfollow = new LinkedList<>();
        User followingUserObj=RegisteredUsers.get(followingUser);
        for (String user : users) {
            if ((followingUserObj!=null) && (followingUserObj.getFollowing().contains(user))) {
                RegisteredUsers.get(followingUser).getFollowing().remove(user);//remove from the follower's list
                successfulUnfollow.add(user);

                RegisteredUsers.get(user).getFollowers().remove(followingUser);//remove from the followed list
            }
        }
        return successfulUnfollow;
    }



}










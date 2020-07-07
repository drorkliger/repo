package bgu.spl.net.api;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.messages.*;
import bgu.spl.net.srv.dataBase;
import java.util.LinkedList;
import java.util.List;


public class BGSProtocol implements BidiMessagingProtocol<Message> {

    private boolean shouldTerminate = false;
    private Connections<Message> connectionsReference;
    private int connectionId;
    private dataBase dataBaseReference;

    public BGSProtocol (dataBase database)//shared object - database
    {
        this.dataBaseReference=database;
    }

    @Override
    public void start(int connectionId, Connections<Message> connections) {
        this.connectionId=connectionId;
        this.connectionsReference=connections;
    }

    @Override
    public void process(Message message) {
        short opCode=message.getOpCode();
        String userName=null;
        String password;
        User user;
        switch (opCode){
//--------------------------------------REGISTER------------------------------------------------------------------------
            case 1:
                synchronized (dataBaseReference.getRegisterLock()) {
                    boolean isRegistered;
                    userName = ((RegisterMessage) message).getUsername();
                    password = ((RegisterMessage) message).getPassword();
                    if (dataBaseReference.getUserByUserName(userName) == null)
                        isRegistered = false;
                    else
                        isRegistered = true;


                    if (isRegistered) {
                        sendErrorMsg(connectionId, opCode);
                    } else {
                        Message ackMessage = new ACKMessage(opCode);
                        connectionsReference.send(connectionId, ackMessage);
                        dataBaseReference.addUnregisteredUser(userName, password);
                        dataBaseReference.setByOrder(userName);
                    }
                }
                    break;
//-------------------------------------LOGIN----------------------------------------------------------------------------
            case 2: synchronized (dataBaseReference.getLoginLock()) {
                    userName = ((LoginMessage) message).getUsername();
                    password = ((LoginMessage) message).getPassword();

                    if ((dataBaseReference.getUserByUserName(userName) != null) && (!dataBaseReference.getUserByUserName(userName).isLogged()
                            & dataBaseReference.getUserByUserName(userName).getPassword().equals(password) & dataBaseReference.getUsernameOfConnectionId(connectionId) == null)) {//if registered but not logged in and password is correct
                        dataBaseReference.getUserByUserName(userName).setLogged(true);
                        dataBaseReference.getUserByUserName(userName).setConnectionId(connectionId);
                        dataBaseReference.setConnectionIdUserName(connectionId, userName);

                        while (dataBaseReference.getUserByUserName(userName).getNotificationQueue().size() != 0) {
                            connectionsReference.send(dataBaseReference.getUserByUserName(userName).getConnectionId(), dataBaseReference.getUserByUserName(userName).getNotificationMessage());
                        }
                        Message ackMessage = new ACKMessage(opCode);
                        connectionsReference.send(connectionId, ackMessage);
                    } else {
                        sendErrorMsg(connectionId, opCode);//method we created to send errors
                    }
                }
                    break;
//-----------------------------------------LOGOUT------------------------------------------------------------------------
            case 3: if(dataBaseReference.getUsernameOfConnectionId(connectionId)==null) {
                        sendErrorMsg(connectionId, opCode);
                        break;
                        }
                        userName=dataBaseReference.getUsernameOfConnectionId(connectionId);
                    synchronized (dataBaseReference.getUserByUserName(userName).getUserLock()) {

                        if ((dataBaseReference.getUserByUserName(userName) != null) && dataBaseReference.getUserByUserName(userName).isLogged()) {
                            Message ackMsg = new ACKMessage(opCode);
                            dataBaseReference.removeConnectionId(connectionId);
                            dataBaseReference.getUserByUserName(userName).setConnectionId(-1);

                            dataBaseReference.getUserByUserName(userName).setLogged(false);
                            connectionsReference.send(connectionId, ackMsg);
                            shouldTerminate = true;
                            connectionsReference.disconnect(connectionId);

                        } else
                            sendErrorMsg(connectionId, opCode);
                    }
                break;
//------------------------------------------FOLLOW-----------------------------------------------------------------------
            case 4: if(dataBaseReference.getUsernameOfConnectionId(connectionId)==null) {
                        sendErrorMsg(connectionId, opCode);
                        break;
                    }
                userName=dataBaseReference.getUsernameOfConnectionId(connectionId);
                    int toFollow=((FollowMessage)message).getToFollow();
                    List<String> users=((FollowMessage)message).getUsers();

                    if ((toFollow==0 & dataBaseReference.getUserByUserName(userName)!=null) && dataBaseReference.getUserByUserName(userName).isLogged()) {
                        List<String> successfulFollow=dataBaseReference.addFollowUsers(userName,users);
                        if (successfulFollow.size() > 0) {
                            Message ackMsg = new ACKMessage(opCode);
                            ((ACKMessage) ackMsg).setUserNameList(successfulFollow);
                            ((ACKMessage) ackMsg).setNumOfUsers((short) successfulFollow.size());
                            connectionsReference.send(connectionId, ackMsg);
                        } else {
                            sendErrorMsg(connectionId,opCode);
                        }
                    }

                    if((toFollow==1 & dataBaseReference.getUserByUserName(userName)!=null) && dataBaseReference.getUserByUserName(userName).isLogged()) {
                        List<String> successfulUnFollow=dataBaseReference.removeUnFollowUsers(userName,users);
                        if (successfulUnFollow.size()>0)
                        {
                            Message ackMsg = new ACKMessage(opCode);
                            ((ACKMessage) ackMsg).setUserNameList(successfulUnFollow);
                            ((ACKMessage) ackMsg).setNumOfUsers((short) successfulUnFollow.size());
                            connectionsReference.send(connectionId, ackMsg);

                        }
                        else {
                            sendErrorMsg(connectionId,opCode);
                        }
                    }
                break;
//---------------------------------------------POST---------------------------------------------------------------------


            case 5: //synchronized ()
                    userName=dataBaseReference.getUsernameOfConnectionId(connectionId);
                    int recieverId=-1;
                    List<String> usersGot=new LinkedList<>();
                     String content=((PostMessage)message).getContent();
                     NotificationMessage notificationMsg=new NotificationMessage((byte)1,userName,content);//1 for POST
                    if(userName!=null && dataBaseReference.getUserByUserName(userName).isLogged())//second check is extra because we found his name by id
                     {
                         for(String followerName:dataBaseReference.getUserByUserName(userName).getFollowers())
                         {
                             synchronized (dataBaseReference.getUserByUserName(followerName).getUserLock()) {
                                 if (dataBaseReference.getUserByUserName(followerName).isLogged()) {
                                     int id = dataBaseReference.getUserByUserName(followerName).getConnectionId();
                                     connectionsReference.send(id, notificationMsg);
                                 } else
                                     dataBaseReference.getUserByUserName(followerName).addNotificationMessage(notificationMsg);
                                 usersGot.add(followerName);
                             }
                         }
                         dataBaseReference.getUserByUserName(userName).addPost((PostMessage) message);
                         Message ackMsg = new ACKMessage(opCode);
                         connectionsReference.send(connectionId, ackMsg);
                     }
                    else
                        sendErrorMsg(connectionId,opCode);


                    String [] splittedContent=content.split("@");
                    int startingIndex=1;
                    if(content.charAt(0)=='@')
                        startingIndex=0;//check for the first word until @, may start diffrently
                    for(int i=startingIndex;i<splittedContent.length;i++)//String string:splittedContent)
                    {
                        int temp = splittedContent[i].indexOf(' ');
                        String tempString;

                        if(temp!=-1) //means there is more than one word
                            tempString = splittedContent[i].substring(0, temp);
                        else
                            tempString=splittedContent[i];

                            if (dataBaseReference.getUserByUserName(tempString) != null && !usersGot.contains(tempString)) {
                                synchronized (dataBaseReference.getUserByUserName(tempString).getUserLock()) {
                                    if (dataBaseReference.getUserByUserName(tempString).isLogged() && !dataBaseReference.getUserByUserName(userName).getFollowers().contains(tempString)) {
                                        recieverId = dataBaseReference.getUserByUserName(tempString).getConnectionId();
                                        connectionsReference.send(recieverId, notificationMsg);
                                    } else
                                        dataBaseReference.getUserByUserName(tempString).addNotificationMessage(notificationMsg);
                                }
                            }

                    }
                break;
//----------------------------------------------PM---------------------------------------------------------------------
            case 6: userName=dataBaseReference.getUsernameOfConnectionId(connectionId);
                    String recieverName=((PMMessage)message).getUsername();
                    content=((PMMessage)message).getContent();
                    notificationMsg=new NotificationMessage((byte)0,userName,content);//0 for PM

                    if((userName!=null && dataBaseReference.getUserByUserName(userName).isLogged()) & (dataBaseReference.getUserByUserName(recieverName)!=null )) {

                        synchronized (dataBaseReference.getUserByUserName(recieverName).getUserLock()) {
                            dataBaseReference.getUserByUserName(userName).addPM((PMMessage) message);
                            if (dataBaseReference.getUserByUserName(recieverName).isLogged()) {
                                recieverId = dataBaseReference.getUserByUserName(recieverName).getConnectionId();
                                connectionsReference.send(recieverId, notificationMsg);
                            } else
                                dataBaseReference.getUserByUserName(recieverName).addNotificationMessage(notificationMsg);
                            Message ackMessage = new ACKMessage(opCode);
                            connectionsReference.send(connectionId, ackMessage);
                        }
                    }
                    else
                        sendErrorMsg(connectionId, opCode);

                break;


//-----------------------------------------USERLIST---------------------------------------------------------------------
            case 7: userName=dataBaseReference.getUsernameOfConnectionId(connectionId);
                    if(userName!=null && dataBaseReference.getUserByUserName(userName).isLogged())
                    {
                        List <String> usersList=dataBaseReference.getRegisteredByOrder();
                        int numOfUsers=usersList.size();
                        Message ackMsg=new ACKMessage(opCode);
                        ((ACKMessage) ackMsg).setUserNameList(usersList);
                        ((ACKMessage) ackMsg).setNumOfUsers((short)numOfUsers);

                        connectionsReference.send(connectionId,ackMsg);
                    }
                    else
                        sendErrorMsg(connectionId,opCode);
                break;

//------------------------------------------STAT------------------------------------------------------------------------
            case 8: userName=dataBaseReference.getUsernameOfConnectionId(connectionId);
                    String userInfoName=((StatMessage)message).getUserName();//the name of the user we get info about
                    if(userName!=null && dataBaseReference.getUserByUserName(userName).isLogged() & dataBaseReference.getUserByUserName(userInfoName)!=null)
                    {
                        Message ackMsg=new ACKMessage(opCode);
                        int numOfPosts=dataBaseReference.getUserByUserName(userInfoName).getPosts().size();
                        int numOfFollowers=dataBaseReference.getUserByUserName(userInfoName).getFollowers().size();
                        int numOfFollowings=dataBaseReference.getUserByUserName(userInfoName).getFollowing().size();
                        ((ACKMessage) ackMsg).setNumPosts((short)numOfPosts);
                        ((ACKMessage) ackMsg).setNumFollowers((short)numOfFollowers);
                        ((ACKMessage) ackMsg).setNumFollowing((short)numOfFollowings);

                        connectionsReference.send(connectionId,ackMsg);
                    }
                    else
                        sendErrorMsg(connectionId,opCode);

                break;
        }


    }

    private void sendErrorMsg(int connectionId, short opCode)//makes new error message and send it
    {
        Message errorMessage = new ERRORMessage(opCode);
        connectionsReference.send(connectionId, errorMessage);
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}

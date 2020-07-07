package bgu.spl.net.api;

import bgu.spl.net.api.Message;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.messages.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MessageEncoderDecoderImp implements MessageEncoderDecoder<Message> {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private byte [] opBytes=new byte[2];
    private int numOfBytes = 0;
    private int len = 0;
    boolean first=true;
    private int zeroCounter = -1;
    private short Opcode = 0;
    private Message message;
    private ByteBuffer byteBuffer;
    private String s=null;
    private String[] strings;
    private boolean hasArraySet=false;
    private boolean hasFollowSet=false;
    private int index;

/*
    //----------NEW ADDITION //--//--//--//--//--//--//--//--//--//--//--//--//--//--//--//--//--//--//--//--//--//--//
    private boolean messageInitialized=false;
    private byte[] foolowOrUnfollow= new byte[2];

    private boolean followersSet=false;
    private int indNumOfFollowers=0;
    private byte[]numOfFollowers=new byte[2];
    private boolean ignorefirstByte=false;
    private boolean followingfine =false;


    int ind=0;
    //------------------------------------------------------------------------------------------------------------------
*/

    @Override
    public Message decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
/*        if (nextByte == '\n') {
            return popString();
        }*/


        if (nextByte == 0 & !first)//decrease the number of zeros
            zeroCounter--;
        //len++;
        if (first)
            first = false;


        if (numOfBytes < 2) {//first 2 bytes go to the op-array
            opBytes[numOfBytes] = nextByte;
            numOfBytes++;
        }

        if (numOfBytes > 2 & nextByte!=0) {//starts adding bytes to the Main array after knowing the opcode
            pushByte(nextByte);
        }

        if (numOfBytes == 2)//after 2 bytes we can know the op code
        {
            Opcode = bytesToShort(opBytes);
            numOfBytes++;
        }


        switch (Opcode) {//case for every kind of message by its op-code
            case 1: {
                if (message == null) {//initialing the message king and its zeros amount
                    message = new RegisterMessage();
                    zeroCounter = ((RegisterMessage) message).getNumOfZeros();
                    strings=new String[2];
                }
                if(zeroCounter==1 & nextByte==0)
                {
                    s = popString();
                    strings[0]=s;
                    clearBytes();
                    len=0;
                }
                if (zeroCounter == 0) {
                    s = popString();
                    strings[1]=s;
                    ((RegisterMessage) message).setUsername(strings[0]);
                    ((RegisterMessage) message).setPassword(strings[1]);
                }
                break;
            }
            case 2: {
                if (message == null) {
                    message = new LoginMessage();
                    zeroCounter = ((LoginMessage) message).getNumOfZeros();
                    strings=new String[2];
                }
                if(zeroCounter==1 & nextByte==0)
                {
                    s = popString();
                    strings[0]=s;
                    clearBytes();
                    len=0;
                }
                if (zeroCounter == 0) {
                    s = popString();
                    strings[1]=s;
                    ((LoginMessage) message).setUsername(strings[0]);
                    ((LoginMessage) message).setPassword(strings[1]);
                }
                break;
            }
            case 3: {
                if (message == null) {
                    message = new LogoutMessage();
                    zeroCounter = ((LogoutMessage) message).getNumOfZeros();
                }
                break;
            }


            //-------------FOLLOW--------------
            case 4: {

                if (message == null) {
                    message = new FollowMessage();
                 //   index=0;//remember the next index to set in the array
                    bytes[0]=0;
                    len=1;
                }
                if((len==2 | (len==1 & zeroCounter==-3))&!hasFollowSet)
                {
                    //s=popString();
                    byte [] follow=new byte[2];
                    follow[0]=bytes[0];
                    follow[1]=nextByte;
                    short toFollow=bytesToShort(follow);
                    int temp=(int)toFollow;
                    ((FollowMessage) message).setToFollow(temp);
                    clearBytes();
                    len=0;
                    hasFollowSet=true;
                    break;
                }

                if ((len == 2 | (len==1 & zeroCounter<=-3)) & (!hasArraySet)) {//cutting the follow byte and the numofusers 2 bytes
                    byte [] usersnum=new byte[2];
                    usersnum[0]=bytes[0];
                    usersnum[1]=bytes[1];
                    if(len==1) {
                        usersnum[0]=0;
                        usersnum[1] = nextByte;
                    }
                    short userNum=bytesToShort(usersnum);
                    zeroCounter = (int)userNum;
                    strings=new String[zeroCounter];
                    clearBytes();
                    len=0;
                    hasArraySet=true;
                }
                if(nextByte==0 & hasArraySet)
                {
                    s=popString();
                    ((FollowMessage) message).addUser(s);
                    clearBytes();
                    len=0;
                }
               // if (zeroCounter == 0) {//returning list of users at the end of the decoding
                //    s = new String(bytes, 3, len, StandardCharsets.UTF_8);//makes string out of the bytes array without the first bytes
                 //   strings = s.split("0");
                  //  ((FollowMessage) message).addUsers(strings);
              //  }

                break;
            }

            case 5: {//-----------------------------------------------------NEEDS TO BE CHECKED----------------------------------------------
                if (message == null) {
                    message = new PostMessage();
                    zeroCounter = ((PostMessage) message).getNumOfZeros();
                    strings=new String[1];
                }
                if (zeroCounter == 0) {
                    s = popString();
                    //strings = s.split("0");
                    ((PostMessage) message).setContent(s);
                }
                break;
            }
            case 6: {
                if (message == null) {
                    message = new PMMessage();
                    zeroCounter = ((PMMessage) message).getNumOfZeros();
                    strings=new String[2];
                    len=0;
                }
                if(zeroCounter==1 & nextByte==0)
                {
                    s = popString();
                    strings[0]=s;
                    clearBytes();
                    len=0;
                }
                if (zeroCounter == 0) {
                    s = popString();
                    strings[1]=s;
                    ((PMMessage) message).setUsername(strings[0]);
                    ((PMMessage) message).setContent(strings[1]);
                }
                break;
            }

            case 7: {
                if (message == null) {
                    message = new UserListMessage();
                    zeroCounter = ((UserListMessage) message).getNumOfZeros();
                }
                break;
            }

            case 8: {
                if (message == null) {
                    message = new StatMessage();
                    zeroCounter = ((StatMessage) message).getNumOfZeros();
                    strings=new String[1];
                }
                if (zeroCounter == 0) {
                    strings[0] = popString();
                    ((StatMessage) message).setUserName(strings[0]);
                }

            }

                break;

        }

        if (zeroCounter == 0) {
            s = null;
            bytes = new byte[1 << 10];
            opBytes = new byte[2];
            numOfBytes = 0;
            zeroCounter = -1;
            Opcode = 0;
            len = 0;
            Message returnMessage = message;
            message = null;
            hasFollowSet=false;
            hasArraySet=false;

            return returnMessage;

        }

        /*if(followingfine==true)
        {
            ignorefirstByte=false;//new addition
            messageInitialized=false;//new addition
            followersSet=false;//new addition
            indNumOfFollowers=0;//new addition
            ind=0;
            Message returnMessage = message;
            message = null;
            followingfine=false;
            return returnMessage;
        }*/
                return null; //not a message yet

        }


    //complete by them
    @Override
    public byte[] encode(Message message) {
        byte[] encodedMsg=null;
        byte [] msgOpBytes=null;
        byte [] temp=null;
        int msgsize;

        short opCode=message.getOpCode();
        byte [] opBytes=shortToBytes(opCode);

        switch (opCode) {
            case 9:
                byte[] content = ((NotificationMessage) message).getContent().getBytes();
                byte[] postingUser = ((NotificationMessage) message).getPostingUser().getBytes();
                msgsize = content.length + postingUser.length + 5;
                encodedMsg = new byte[msgsize];
                encodedMsg[0] = opBytes[0];
                encodedMsg[1] = opBytes[1];
                encodedMsg[2] = ((NotificationMessage) message).getNT();

                for (int i = 0; i < postingUser.length; i++) {
                    encodedMsg[i + 3] = postingUser[i];
                }
                encodedMsg[postingUser.length + 4] = 0;
                for (int i = 0; i < content.length; i++) {
                    encodedMsg[i + postingUser.length + 4] = content[i];
                }
                break;


            case 10: {
                msgOpBytes = shortToBytes(((ACKMessage) message).getMsgOpCode());
                byte[] optional = null;
                List<byte[]> optional2 = new LinkedList<>();
                int optionalSize = 0;
                int numOfChars = 0;
                int runningIndex = 6;
                short mop = ((ACKMessage) message).getMsgOpCode();

                if (mop == 1 | mop == 2 | mop == 3 | mop == 5 | mop == 6) {
                    encodedMsg = new byte[4];
                    encodedMsg[0] = opBytes[0];
                    encodedMsg[1] = opBytes[1];
                    encodedMsg[2] = msgOpBytes[0];
                    encodedMsg[3] = msgOpBytes[1];
                }

                if (mop == 4 | mop == 7) {
                    numOfChars = numOfChars(((ACKMessage) message).getUserNameList());//count the chars and zeros of all the users
                    encodedMsg = new byte[6 + numOfChars];
                    encodedMsg[0] = opBytes[0];
                    encodedMsg[1] = opBytes[1];
                    encodedMsg[2] = msgOpBytes[0];
                    encodedMsg[3] = msgOpBytes[1];
                    temp = shortToBytes(((ACKMessage) message).getNumOfUsers());
                    encodedMsg[4] = temp[0];
                    encodedMsg[5] = temp[1];


                    for (int i = 0; i < ((ACKMessage) message).getNumOfUsers(); i++) {
                        optional2.add(((ACKMessage) message).getUserNameList().get(i).getBytes());
                        for (int j = 0; j < optional2.get(i).length; j++) {
                            encodedMsg[runningIndex] = optional2.get(i)[j];
                            runningIndex++;
                        }
                        encodedMsg[runningIndex] = 0;//adds zero between 2 usernames
                        runningIndex++;
                    }
                }


                if (mop == 8) {
                    encodedMsg = new byte[10];
                    encodedMsg[0] = opBytes[0];
                    encodedMsg[1] = opBytes[1];
                    encodedMsg[2] = msgOpBytes[0];
                    encodedMsg[3] = msgOpBytes[1];
                    temp = shortToBytes(((ACKMessage) message).getNumPosts());
                    encodedMsg[4] = temp[0];
                    encodedMsg[5] = temp[1];
                    temp = shortToBytes(((ACKMessage) message).getNumFollowers());
                    encodedMsg[6] = temp[0];
                    encodedMsg[7] = temp[1];
                    temp = shortToBytes(((ACKMessage) message).getNumFollowing());
                    encodedMsg[8] = temp[0];
                    encodedMsg[9] = temp[1];
                }
            }
            break;


            case 11: {
                encodedMsg = new byte[4];
                msgOpBytes = shortToBytes(((ERRORMessage) message).getMsgOpCode());
                encodedMsg[0] = opBytes[0];
                encodedMsg[1] = opBytes[1];
                encodedMsg[2] = msgOpBytes[0];
                encodedMsg[3] = msgOpBytes[1];
            }
            break;
        }

        return encodedMsg;
    }

    //----------------------------------------------------------------------------------------------

    private void pushByte(byte nextByte) {//adds byte dynamically
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    private String popString() {//makes string out of bytes array
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        return result;
    }


    public short bytesToShort(byte[] byteArr) {//turn 2 bytes array to short type
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num) {//turn short type to 2 bytes array
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    private int numOfChars(List <String> users)//sum the number of all the chars in a string chain and adds 1 for each string
    {
        int charCounter=0;
        for(String user:users)
            charCounter+=user.length();
        charCounter+=users.size();//number of zeros
        return charCounter;
    }

    public String[] splitStringBy0(String s)
    {
        int numberOf0=0;
        for(int i=0;i<s.length();i++)
        {
            if(s.charAt(i)=='0')
                numberOf0++;
        }
        String [] partedBy0=new String[numberOf0];
        String field=new String();
        int j=0;
        for(int i=0;i<s.length();i++) {
         if(s.charAt(i)!=0)
            field = field + s.charAt(i);
         else
         {
             partedBy0[j]=field;
             j++;
         }
        }
        return partedBy0;
    }

    private void clearBytes()
    {
        for(int i=0; i<bytes.length;i++) {
            bytes[i] = 0;
        }
    }

}
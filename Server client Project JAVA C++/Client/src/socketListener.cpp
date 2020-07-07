#include <mutex>
#include <socketListener.h>
#include "socketListener.h"

socketListener::socketListener(ConnectionHandler &connectionHandler):terminate(false) {
    connectionHandlerRefference=&connectionHandler;
    string toPrint="";
    string temp="";

}

void socketListener::run() {


    connectionHandlerRefference->connect();

    while(!terminate)
    {
        char opCode[2]; // represent if ack or notification or error
        connectionHandlerRefference->getBytes(opCode,2);
        short Op=bytesToShort(opCode);

        if(Op==(short)9)//NOTIFICATION
        {
            toPrint+="NOTIFICATION ";
            char NT[1];
            connectionHandlerRefference->getBytes(NT,1);
            if(NT[0]==0)
                toPrint+="PM ";
            else{
                toPrint+="Public ";
            }
            string PostingUser="";
            connectionHandlerRefference->getLine(PostingUser);
            PostingUser=PostingUser.substr(0,PostingUser.size()-1);
            toPrint+=PostingUser+" ";
            string content="";
            connectionHandlerRefference->getLine(content);
            content=content.substr(0,content.size()-1);
            toPrint+=content;
        }

        if(Op==(short)10)
        {
            toPrint+="ACK ";
            char messageOp[2];
            connectionHandlerRefference->getBytes(messageOp,2);
            short msgOp=bytesToShort(messageOp);
            temp = to_string(msgOp);//converting the msgOp to string
            toPrint+=temp;

            if(msgOp==(short)2)
                connectionHandlerRefference->setLoggedIn(true);

            if(msgOp==(short)3) {
                connectionHandlerRefference->setIndicator(-1);
                terminate = true;
            }

            if(msgOp==(short)4 | msgOp==(short)7)
            {
                char numOfUsers[2];
                connectionHandlerRefference->getBytes(numOfUsers,2);
                short usersAmount=bytesToShort(numOfUsers);
                temp=to_string(usersAmount);
                toPrint+=" "+temp;//adding the number of users as astring
                for(int i=0;i<usersAmount;i++) {
                    string userName = "";
                    connectionHandlerRefference->getLine(userName);
                    userName = userName.substr(0, userName.size() - 1);
                    toPrint += " " + userName;
                    userName = "";
                }
            }

            if(msgOp==(short)8)
            {
                char numOfposts[2];
                connectionHandlerRefference->getBytes(numOfposts,2);
                short postAmount=bytesToShort(numOfposts);
                string posts=to_string(postAmount);
                toPrint+=" "+posts;//adding the number of posts as a string
                //^^^^^^^^^^^^^^^^^numOfPosts ^^^^^^^^^^^^^^^^^^^^^//
                char numOfFollowers[2];
                connectionHandlerRefference->getBytes(numOfFollowers,2);
                short followersAmount=bytesToShort(numOfFollowers);
                string followers=to_string(followersAmount);
                toPrint+=" "+followers;//adding the number of followers as a string
                //^^^^^^^^^^^^^^^^^numOfFollowers ^^^^^^^^^^^^^^^^^^^^^//
                char numOfFollowing[2];
                connectionHandlerRefference->getBytes(numOfFollowing,2);
                short followingAmount=bytesToShort(numOfFollowing);
                string following=to_string(followingAmount);
                toPrint+=" "+following;//adding the number of followers as a string
                //^^^^^^^^^^^^^^^^^numOfFollowing ^^^^^^^^^^^^^^^^^^^^^//
            }

        }

        if(Op==(short)11)
        {
            toPrint+="ERROR ";
            char msgOpCode[2]; // represent if ack or notification or error
            connectionHandlerRefference->getBytes(msgOpCode,2);
            short msgOp=bytesToShort(msgOpCode);
            string messageOp=to_string(msgOp);
            toPrint+=messageOp;//adding the msg op code to the string
        }
        std::cout<<toPrint<<endl;
        toPrint="";
    }

}



short socketListener:: bytesToShort(char* bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}
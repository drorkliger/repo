#include <string>
#include <iostream>
#include <vector>
#include "keyboardInput.h"
#include <zconf.h>
#include <mutex>
#include <keyboardInput.h>
#include <iostream>
#include "ConnectionHandler.h"

using namespace std;

keyboardInput::keyboardInput(ConnectionHandler &connectionHandler) {
    connectionHandlerReference=&connectionHandler;
}

void keyboardInput::run() {

    //endless loop, shutdown only when the program shuts down

    while (1) {


        //input from keyboard

        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string input(buf);



        //part one, parse message
        //Cut message into words
        stringstream cutter(input);
        string token;

        //will contain words of message
        vector<string> forEncode;
        while(getline(cutter, token,' '))
        {
            forEncode.push_back(token);
        }

        // encode message by its type
        string type=forEncode[0];

        char opCode[2];


//--------encode REGISTER--------------

        if( type.compare("REGISTER")==0)
        {
            shortToBytes(1,opCode);
            connectionHandlerReference->sendBytes(opCode,2);//sends the op code
            connectionHandlerReference->sendLine(forEncode[1]);//sends user name
            connectionHandlerReference->sendLine(forEncode[2]);//sends password
        }
//-------encode LOGIN-----------------
       if( type.compare("LOGIN")==0)
        {
            shortToBytes(2,opCode);
            connectionHandlerReference->sendBytes(opCode,2);//sends the op code

            connectionHandlerReference->sendLine(forEncode[1]);//send user name
            connectionHandlerReference->sendLine(forEncode[2]);//sends password
        }

//------encode LOGOUT-----------------

        if( type.compare("LOGOUT")==0)
        {
            opCode[0]=0;
            opCode[1]=3;
            connectionHandlerReference->sendBytes(opCode,2);//sends the op code
            if(connectionHandlerReference->isLoggedIn())
                connectionHandlerReference->setIndicator(1);
            while (connectionHandlerReference->getIndicator()==1)
                {

                }

            if(connectionHandlerReference->getIndicator()==-1)
                break;

        }

//------encode FOLLOW-----------------

        if( type.compare("FOLLOW")==0)
        {
            opCode[0]=0;
            opCode[1]=4;
            connectionHandlerReference->sendBytes(opCode,2);

            char FT[1];//FOLLOW TYPE
            string s=forEncode[1];
            if(s.compare("0")==0)
                FT[0]=0;
            else
                FT[0]=1;
            connectionHandlerReference->sendBytes(FT,1);

            short NumUsers=(short)stoi(forEncode[2]);
            char numusers[2];
            shortToBytes(NumUsers,numusers);
            connectionHandlerReference->sendBytes(numusers,2);

            for(int i=3;i<forEncode.size();i++)
            {
                connectionHandlerReference->sendLine(forEncode[i]);
            }
        }

//-----encode POST Message-----------
        if( type.compare("POST")==0)
        {
            opCode[0]=0;
            opCode[1]=5;
            connectionHandlerReference->sendBytes(opCode,2);

            string content=input.substr(5);//cuts the word POST from the input string
            connectionHandlerReference->sendLine(content);
        }

//----encode PM Message--------------
        if(type.compare("PM")==0)
        {
            opCode[0]=0;
            opCode[1]=6;
            connectionHandlerReference->sendBytes(opCode,2);

            connectionHandlerReference->sendLine(forEncode[1]);//send user name
            int size=forEncode[1].size();
            string content=input.substr(4+size);//cuts the PM and username from the input to remain the content only

            connectionHandlerReference->sendLine(content);//sends content
        }
//----encode UserList----------------
        if(type.compare("USERLIST")==0)
        {
            opCode[0]=0;
            opCode[1]=7;
            connectionHandlerReference->sendBytes(opCode,2);//sends op code
        }
//----encode STAT--------------------
        if(type.compare("STAT")==0)
        {
            opCode[0]=0;
            opCode[1]=8;
            connectionHandlerReference->sendBytes(opCode,2);//sends op code

            connectionHandlerReference->sendLine(forEncode[1]);
        }
    }



}

short keyboardInput::bytesToShort(char *bytesArr) {
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}

void keyboardInput::shortToBytes(short num, char *bytesArr) {
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

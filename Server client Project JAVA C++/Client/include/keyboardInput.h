#ifndef UNTITLED1_KEYBOARDINPUT_H
#define UNTITLED1_KEYBOARDINPUT_H

#include "ConnectionHandler.h"

using namespace std;

class keyboardInput {

public:
    keyboardInput(ConnectionHandler &connectionHandler);
    void run();



private:
    ConnectionHandler *connectionHandlerReference;
    short bytesToShort(char* bytesArr);
    void shortToBytes(short num, char* bytesArr);


};


#endif //UNTITLED1_KEYBOARDINPUT_H
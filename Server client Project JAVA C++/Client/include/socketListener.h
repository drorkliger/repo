#ifndef UNTITLED1_SOCKETLISTENER_H
#define UNTITLED1_SOCKETLISTENER_H


#include "ConnectionHandler.h"

using namespace std;
class socketListener {
public:
    socketListener(ConnectionHandler &connectionHandler);
    void run();

private:
    ConnectionHandler *connectionHandlerRefference;
    bool terminate;
    string toPrint;
    string temp;
    short bytesToShort(char* bytesArr);
};


#endif //UNTITLED1_SOCKETLISTENER_H
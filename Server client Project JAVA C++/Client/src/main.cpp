#include <iostream>
#include "keyboardInput.h"
#include "ConnectionHandler.h"
#include "socketListener.h"
#include <thread>

int main(int argc, char *argv[]) {

    std::string host = argv[1];
    short port = atoi(argv[2]);

    //creating connection handler with specific host and port from input
    ConnectionHandler connectionHandler(host, port);


    keyboardInput keyboardInput(connectionHandler);
    socketListener socketListener(connectionHandler);


    std::thread th1(&keyboardInput::run,keyboardInput); // we use std::ref to avoid creating a copy of the Task object
    std::thread th2(&socketListener::run,socketListener);
    th2.join();//first we want to connect to the server and only then start sending commands
    th1.join();

    return 0;
}

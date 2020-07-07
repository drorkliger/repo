=========================================================================================
			  Server client Project by Dror Kliger
=========================================================================================

In this project I implemented a simple social network server and client. The server is 
implemented in JAVA and the client is implemented in C++.
The communication between the server and the clients is performed using a binary
communication protocol. A registered user is able to follow other users and post messages. 
The implementation of the server is based on the Thread-Per-Client (TPC) and Reactor 
servers. 
I implemented BGS (Ben Gurion Social) Protocol that emulates a simple social network.
Users need to register to the service. Once registered, they are able to post messages and 
follow other users. It is a binary protocol that uses pre defined message
length for different commands. The commands are defined by an opcode, a short number
at the start of each message. For each command, a different length of data needs to be
read according to it’s specifications.
I didn't use extern data-base in order to save data (Users, Passwords, Messages, ect...).
Instead I just saved the information in the storage allocated to the JVM while running
the server.

=========================================================================================
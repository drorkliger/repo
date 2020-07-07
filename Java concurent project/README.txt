=========================================================================================
		  Java concurrent programming project by Dror Kliger
=========================================================================================

The main subject of the project is to practice concurrent programming in Java environment. 
This project emphasizes the use of Java Threads, Java Synchronization, Lambdas, and 
Callbacks.
In the project I implemented a Micro-Service framework, which I used to implement an 
online book store with a delivery option.
The end-to-end description of the system is simple. A customer connects to the store 
website and orders a book. If the book is available and the customer has enough credit, 
the order is confirmed - the customer pays for the book, and then the book 
should be delivered to his address as soon as possible.

=========================================================================================

This project is composed of two main sections:
1. Building a simple Micro-Service framework.
2. Implementing an online books store application on top of this framework.
The Micro-Service framework consists of two main parts: A Message-Bus and Micro-Services. 
Each Micro-Service is a thread that can exchange messages with other Micro-Services using a 
shared object referred to as the Message-Bus.
The different MicroServices is able to communicate with each other using only a shared
object: A Message-Bus. The Message-Bus supports the sending and receiving of two types of 
events: Broadcast messages, which upon being sent is delivered to every subscriber of the 
specific message type, and Event messages, which upon being sent is delivered to only one 
of its subscribers (in a round robin manner).
The different Micro-Services is able to subscribe for message types they would like to 
receive using the Message-Bus. 
The different Micro-Services do not know of each other’s existence. All they know of is 
messages that were received in their message-queue which is located in the Message-Bus.
 
=========================================================================================

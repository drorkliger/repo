CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -pthread -lboost_filesystem -lboost_thread

all: BGSclient
	g++ -o bin/BGSclient bin/ConnectionHandler.o bin/main.o bin/keyboardInput.o bin/socketListener.o $(LDFLAGS)

BGSclient: bin/ConnectionHandler.o bin/keyboardInput.o bin/socketListener.o bin/main.o
	
bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp
	
bin/main.o: src/main.cpp
	g++ $(CFLAGS) -o bin/main.o src/main.cpp
	
bin/keyboardInput.o: src/keyboardInput.cpp
	g++ $(CFLAGS) -o bin/keyboardInput.o src/keyboardInput.cpp
	
bin/socketListener.o: src/socketListener.cpp
	g++ $(CFLAGS) -o bin/socketListener.o src/socketListener.cpp
	
.PHONY: clean
clean:
	rm -f bin/*

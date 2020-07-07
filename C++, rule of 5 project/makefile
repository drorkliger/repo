CC = g++
CFLAGS  = -g -Wall -std=c++11
LFLAGS  = -L/usr/lib

all: rest

rest: bin/Table.o bin/Action.o bin/Customer.o bin/Restaurant.o bin/Dish.o bin/Main.o
	@echo 'Building target: hello'
	@echo 'Invoking: C++ Linker'
	$(CC) -o bin/rest  bin/Table.o bin/Action.o bin/Customer.o bin/Restaurant.o bin/Dish.o bin/Main.o $(LFLAGS)
	@echo 'Finished building target: rest'
	@echo ' '

bin/Main.o: src/Main.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/Main.o src/Main.cpp

bin/Table.o: src/Table.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/Table.o src/Table.cpp

bin/Restaurant.o: src/Restaurant.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/Restaurant.o src/Restaurant.cpp

bin/Customer.o: src/Customer.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/Customer.o src/Customer.cpp

bin/Dish.o: src/Dish.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/Dish.o src/Dish.cpp

bin/Action.o: src/Action.cpp
	$(CC) $(CFLAGS) -c -Iinclude -o bin/Action.o src/Action.cpp

clean: 
	rm -f bin/*

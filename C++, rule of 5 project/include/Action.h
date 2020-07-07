#ifndef ACTION_H_
#define ACTION_H_

#include <string>
#include <iostream>
//#include "Customer.h"
#include "Table.h"

enum ActionStatus {
    PENDING, COMPLETED, ERROR
};

//Forward declaration
class Restaurant;

class BaseAction {
public:
    BaseAction(); //v
    ActionStatus getStatus() const;
    virtual void act(Restaurant& restaurant) = 0;
    virtual std::string toString() const = 0;
    virtual ~BaseAction();//v
    virtual BaseAction* clone()=0;
protected:
    void complete();//v
    void error(std::string errorMsg);//v
    std::string getErrorMsg() const; //v
private:
    std::string errorMsg;
    ActionStatus status;
};


class OpenTable : public BaseAction {
public:
    OpenTable(int id, std::vector<Customer *> &customersList);
    void act(Restaurant &other);
    std::string toString() const;
    ~OpenTable();
    OpenTable* clone();
    OpenTable(const OpenTable &openTable);
private:
    const int tableId;
    std::vector<Customer *> customers;
};


class Order : public BaseAction {
public:
    Order(int id);
    void act(Restaurant &restaurant);
    std::string toString() const;
    Order* clone();
    ~Order();//check if neccessery
private:
    const int tableId;
};


class MoveCustomer : public BaseAction {
public:
    MoveCustomer(int src, int dst, int customerId);
    void act(Restaurant &restaurant);
    std::string toString() const;
    vector <OrderPair> removeFromOrderList(const vector<OrderPair> &listOfOrders,int index);
    MoveCustomer* clone();
    ~MoveCustomer();//check if neccessery
private:
    const int srcTable;
    const int dstTable;
    const int id;
};


class Close : public BaseAction {
public:
    Close(int id);
    void act(Restaurant &restaurant);
    std::string toString() const;
    Close* clone();
    ~Close();//check if neccessery
private:
    const int tableId;
};


class CloseAll : public BaseAction {
public:
    CloseAll();
    void act(Restaurant &restaurant);
    std::string toString() const;
    CloseAll* clone();
    ~CloseAll();//check if neccessery
private:
};


class PrintMenu : public BaseAction {
public:
    PrintMenu();
    void act(Restaurant &restaurant);
    std::string toString() const;
    PrintMenu* clone();
    ~PrintMenu();//check if neccessery
private:
};


class PrintTableStatus : public BaseAction {
public:
    PrintTableStatus(int id);
    void act(Restaurant &restaurant);
    std::string toString() const;
    PrintTableStatus* clone();
    ~PrintTableStatus();//check if neccessery
private:
    const int tableId;
};


class PrintActionsLog : public BaseAction {
public:
    PrintActionsLog();
    void act(Restaurant &restaurant);
    std::string toString() const;
    PrintActionsLog* clone();
    ~PrintActionsLog();//check if neccessery
private:
};


class BackupRestaurant : public BaseAction {
public:
    BackupRestaurant();
    void act(Restaurant &restaurant);
    std::string toString() const;
    BackupRestaurant* clone();
    ~BackupRestaurant();//check if neccessery
private:
};


class RestoreResturant : public BaseAction {
public:
    RestoreResturant();
    void act(Restaurant &restaurant);
    std::string toString() const;
    RestoreResturant* clone();
    ~RestoreResturant();//check if neccessery

};


#endif
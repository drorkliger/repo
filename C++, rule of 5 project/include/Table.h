#ifndef TABLE_H_
#define TABLE_H_

#include <vector>
#include "Customer.h"
#include "Dish.h"

typedef std::pair<int, Dish> OrderPair;

class Table {
public:
    Table(int t_capacity);//V
    int getCapacity() const;//V
    void addCustomer(Customer* customer);//V
    void removeCustomer(int id);
    Customer* getCustomer(int id);//V
    std::vector<Customer*>& getCustomers();//V
    std::vector<OrderPair>& getOrders();//V
    void order(const std::vector<Dish> &menu);
    void openTable();//V
    void closeTable();
    int getBill();//V
    bool isOpen();//V
    ~Table();
    Table(const Table &t);
    Table& operator=(const Table& other);//copy assignment operator
    Table& operator=(Table&& other);//move assignment operator
    Table(Table &&t);//move constructor
private:
    int capacity;
    bool open;
    std::vector<Customer*> customersList;
    std::vector<OrderPair> orderList;
    //Customer* lastDeleted; //18/11 4:12  for move function
};


#endif
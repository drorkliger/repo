#include "Table.h"
#include <iostream>
#include <vector>
#include "Customer.h"

using namespace std;

//constructor
Table::Table(int t_capacity) : capacity(t_capacity),open(false),customersList(),orderList(){} //18/11 22:29 initilize customer list

//get capacity
int Table::getCapacity() const
{
    return capacity;
}

//is Table open
bool Table::isOpen()
{
    return open;
}

//get customer list
vector<Customer *> &Table::getCustomers() {
    return customersList;
}

//get order list
std::vector<OrderPair> &Table::getOrders() {
    return orderList;
}

//get bill of table
int Table::getBill() {
    int sumToPay=0;
    for(unsigned int i=0;i<orderList.size();i++)
    {
        sumToPay+=orderList[i].second.getPrice();
    }
    return sumToPay;
}

//function open table
void Table::openTable() {
    open=true;
}

// function add customer to the table
void Table::addCustomer(Customer *customer) {
    if (this->customersList.size()<(unsigned int)capacity) //17/11 18:44
        customersList.push_back(customer);
}

void Table::removeCustomer(int id) {
    bool isFound=false;
    int i=0;
    while((!isFound) & ((unsigned int)i<customersList.size()))
    {
        if(customersList[i]->getId()==id)
        {
            isFound=true;
            customersList.erase(customersList.begin()+i);
        }
        i++;
    }
}

//get customer
Customer* Table::getCustomer(int id) {
    bool isFound=false;
    int i=0;
    //finding costumer
    while((!isFound) & ((unsigned int)i<customersList.size())){
        if(customersList[i]->getId()==id)
            return customersList[i];
        i++;
    }
    return nullptr;
}

//function close table
void Table::closeTable() {
    open=false;
    for(auto customer:customersList)
        delete(customer);
    customersList.clear();

    orderList.clear();
}

void Table::order(const std::vector<Dish> &menu)
{
    for(unsigned int i=0;i<customersList.size();i++)
        customersList[i]->order(menu);
}

Table::~Table() {
    //std::vector<Customer*> customersList;
    for(unsigned int i=0;i<customersList.size();i++)
    {
        if(customersList[i]!= nullptr)//18.11 11:35
            delete(customersList[i]);
    }
    customersList.clear();// 18/11 4:01
    orderList.clear(); // maybe not need
}

//copy constructor
Table::Table(const Table &t): capacity(t.capacity), open(t.open),customersList(),orderList()
{
    for(unsigned int i=0;i<t.customersList.size();i++)
        this->customersList.push_back(t.customersList[i]->clone());

    for(unsigned int i=0;i<t.orderList.size();i++)
        this->orderList.push_back(t.orderList[i]);
}

//copy assignment operator
Table& Table::operator=(const Table &other) {
    if(this==&other)
        return *this;

    this->customersList.clear();
    for(unsigned int i=0;i<other.customersList.size();i++)
        this->getCustomers().push_back(other.customersList[i]);

    this->orderList.clear();

    for(unsigned int i=0;i<other.orderList.size();i++)
        this->orderList.push_back(other.orderList[i]);

    return *this;
}

//Move assignment operator
Table &Table::operator=(Table &&other)
{
    if(this!=&other)
    {
        customersList.clear();
        orderList.clear();
        delete(this);//check if it really delete well
        *this=other;
    }
    return *this;
}

//have to check
//move constructor
Table::Table(Table &&t): capacity(t.capacity),open(t.open),customersList(t.customersList),orderList(t.orderList)
{
    *this=t;
    delete(&t);//check if it really delete well
}
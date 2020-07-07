#include "Action.h"
#include "Customer.h"
#include "Table.h"
#include "Dish.h"
#include "Restaurant.h"
#include <iostream>
#include <vector>


extern Restaurant* backup;

using namespace std;

//BaseAction constructor
BaseAction::BaseAction():errorMsg(""),status(PENDING){}

//function return status
ActionStatus BaseAction::getStatus() const {
    return status;
}

//function return error message
std::string BaseAction::getErrorMsg() const {
    return errorMsg;
}

//function assign error message
void BaseAction::error(std::string errorMsg)
{
    status=ERROR;
    this->errorMsg=errorMsg;
}

//function set complete to action
void BaseAction::complete() {
    status=COMPLETED;
}

//virtual function of BaseAction for actions
BaseAction::~BaseAction() {}





///////////////////////Open table//////////////////////////
OpenTable::OpenTable(int id, std::vector<Customer *> &customersList) : BaseAction(),tableId(id), customers(customersList){}


//function open table and assign customers to the table
void OpenTable::act(Restaurant &restaurant) {
    if((restaurant.getTable(tableId) != nullptr) && (!restaurant.getTable(tableId)->isOpen())
                                                    & ((unsigned int)restaurant.getTable(tableId)->getCapacity()>restaurant.getTable(tableId)->getCustomers().size())) {
        restaurant.getTable(tableId)->openTable();
        for (unsigned int i = 0; i < customers.size(); i++) {
            restaurant.getTable(tableId)->addCustomer(customers[i]->clone());
        }
        BaseAction :: complete();
    }
    else {
        BaseAction::error("Table does not exist or is already open");
        cout<<"Error: Table does not exist or is already open"<<endl;
    }
}

//function return to string of open table
std::string OpenTable::toString() const {
    string returnString="";
    string status="";
    if(getStatus()==ERROR)
        status=" Error: Table does not exist or is already open";
    if(getStatus()==PENDING)
        status=" Pending";
    if(getStatus()==COMPLETED)
        status=" Completed";

    returnString+="open "+to_string(tableId);
    for(unsigned int i=0;i<this->customers.size();i++)
        returnString+=" "+customers[i]->getName()+","+customers[i]->getType();
    if(status=="error")
        return returnString+status;
    return returnString+status ;
}

//destractor OpenTable
OpenTable::~OpenTable()
{
    for(unsigned int i=0;i<customers.size();i++)
    {
        delete(customers[i]);
    }
}

//clone OpenTable
OpenTable *OpenTable::clone()
{
    return new OpenTable(*this);
}

OpenTable::OpenTable(const OpenTable &openTable):BaseAction(),tableId(openTable.tableId),customers() {
    if(openTable.getStatus()==COMPLETED)
        complete();
    if(openTable.getStatus()==ERROR)
        error(openTable.getErrorMsg());
    for(const auto customer:openTable.customers)
        customers.push_back(customer->clone());

}


//constructor of order
Order::Order(int id) :BaseAction(),tableId(id) {}

//executing order to table
void Order::act(Restaurant &restaurant) {

    vector<Dish>currentOrder; //vector of all dishes in order
    vector<OrderPair>currentOrderPair; // vector of pairs
    int counter=0;

    //if table excist and table is open
    if(restaurant.getTable(tableId)!=nullptr && restaurant.getTable(tableId)->isOpen()) {
        Table *table = restaurant.getTable(tableId); //table who order

        for (unsigned int i = 0; i < table->getCustomers().size(); i++)
        {
            //the id of the ordering customer
            int idd=restaurant.getTable(tableId)->getCustomers()[i]->getId();
            //int vector of orders id of current customer
            vector <int> orderOFcustomer;
            orderOFcustomer=table->getCustomer(restaurant.getTable(tableId)->getCustomers()[i]->getId())->order(restaurant.getMenu());
            //updatin a vector of dishes from vector of ids of a order
            for(unsigned int j=0;j<orderOFcustomer.size();j++)
            {
                currentOrder.push_back(restaurant.getDish(orderOFcustomer[j]));
            }

            //organzie pairs
            for(unsigned int d=counter;d<currentOrder.size();d++){
                OrderPair *pair=new OrderPair(idd,currentOrder[d]);
                restaurant.getTable(tableId)->getOrders().push_back(*pair);
                currentOrderPair.push_back(*pair);
                counter++;
                delete(pair);
            }

        }
        //function assign complete to the act from BAseActions
        BaseAction :: complete();
        for(unsigned int i=0;i<currentOrder.size();i++)
        {
            cout<<restaurant.getTable(tableId)->getCustomer(currentOrderPair[i].first)->getName();
            cout<<" ordered ";
            cout<<currentOrderPair[i].second.getName()<<endl;
        }
    }
    else {
        BaseAction::error("Table does not exist or is not open");
        cout<<"Error: Table does not exist or is not open"<<endl;
    }

}



std::string Order::toString() const {
    string returnString="";
    string status="";
    returnString+="order "+to_string(tableId);

    if(getStatus()==ERROR)
        status=" Error: Table does not exist or is not open";
    if(getStatus()==PENDING)
        status=" Pending";
    if(getStatus()==COMPLETED)
        status=" Completed";

    returnString+=status;

    return returnString ;
}

//creating (similiar to copy constructor)
Order *Order::clone() {
    return new Order(*this);
}






//constructor of MoveCustomer
MoveCustomer::MoveCustomer(int src, int dst, int customerId) :srcTable(src),dstTable(dst),id(customerId){}


//executing move customer
void MoveCustomer::act(Restaurant &restaurant)
{

    int k=(unsigned int)restaurant.getTable(srcTable)->getOrders().size(); //size of src order list
    //should to it in other function
    if(restaurant.getTable(dstTable)!= nullptr && restaurant.getTable(srcTable)!= nullptr &&
       restaurant.getTable(dstTable)->isOpen() && restaurant.getTable(srcTable)->isOpen() &&
       restaurant.getTable(srcTable)->getCustomer(id)!= nullptr &&
       (unsigned int)restaurant.getTable(dstTable)->getCapacity()>restaurant.getTable(dstTable)->getCustomers().size())
    {
        //moving customer to the other table
        restaurant.getTable(dstTable)->addCustomer(restaurant.getTable(srcTable)->getCustomer(id));
        restaurant.getTable(srcTable)->removeCustomer(id);
        //what orders to move
        for(unsigned int i=0;i<(unsigned int)k;i++)
        {
            if(restaurant.getTable(srcTable)->getOrders()[i].first==id)
            {
                restaurant.getTable(dstTable)->getOrders().push_back(restaurant.getTable(srcTable)->getOrders()[i]);
            }
        }
        restaurant.getTable(srcTable)->getOrders()=this->removeFromOrderList(restaurant.getTable(srcTable)->getOrders(),id);
        if(restaurant.getTable(srcTable)->getCustomers().size()==0)
            restaurant.getTable(srcTable)->closeTable();

        BaseAction :: complete();
    }
    else {
        BaseAction::error("Cannot move customer");
        cout <<"Error: "+getErrorMsg() << endl;
    }
}

//clone (similar to copy constructor)
MoveCustomer *MoveCustomer::clone() {
    return new MoveCustomer(*this);
}

//to string
string MoveCustomer::toString() const {
    string returnString="";
    string status="";

    if(getStatus()==ERROR)
        status=" Error: Cannot move customer";
    if(getStatus()==PENDING)
        status=" Pending";
    if(getStatus()==COMPLETED)
        status=" Completed";

    returnString+="move "+to_string(srcTable)+" "+to_string(dstTable)+" "+to_string(id)+status;

    return returnString;
}

//
vector <OrderPair> MoveCustomer ::removeFromOrderList(const vector<OrderPair> &listOfOrders,int index)
{
    vector <OrderPair> v;
    for(unsigned int i=0;i<listOfOrders.size();i++)
    {
        if(listOfOrders[i].first!=index)
            v.push_back(listOfOrders[i]);
    }
    return v;
}







Close::Close(int id):BaseAction(), tableId(id) {}

void Close::act(Restaurant &restaurant) {
    if(restaurant.getTable(tableId)!= nullptr && restaurant.getTable(tableId)->isOpen())
    {
        cout<<("Table ");
        cout<<(tableId);
        cout<<(" was closed. Bill ");
        cout<<(restaurant.getTable(tableId)->getBill());
        cout<<("NIS")<<endl;

        //restaurant.getTable(tableId)->getCustomers().clear();

        //restaurant.getTable(tableId)->getOrders().clear();
        restaurant.getTable(tableId)->closeTable();
        BaseAction :: complete();
    }
    else {
        BaseAction::error("Table does not exist or is not open");
        cout<<"Error: Table does not exist or is not open"<<endl;
    }
}

std::string Close::toString() const {

    string returnString="";
    string status="";
    returnString+="close "+to_string(tableId);

    if(getStatus()==ERROR)
        status=" Error";
    if(getStatus()==PENDING)
        status=" Pending";
    if(getStatus()==COMPLETED)
        status=" Completed";

    returnString+=status;

    return returnString ;
}

Close *Close::clone() {
    return new Close(*this);
}










CloseAll::CloseAll():BaseAction() {}

void CloseAll::act(Restaurant &restaurant)
{
    for(int i=0;i<restaurant.getNumOfTables();i++)
    {
        if(restaurant.getTable(i)->isOpen()) {
            Close *close=new Close(i);
            close->act(restaurant);
            delete(close);
        }
    }
    BaseAction :: complete();
}

std::string CloseAll::toString() const {
    return "";
}

CloseAll *CloseAll::clone() {
    return new CloseAll(*this);
}



PrintMenu::PrintMenu():BaseAction() {}

void PrintMenu::act(Restaurant &restaurant)
{
    string type;
    for(unsigned int i=0;i<restaurant.getMenu().size();i++)
    {
        cout<<restaurant.getMenu()[i].getName() +" ";
        if(restaurant.getMenu()[i].getType()==ALC)
            cout<<"ALC";
        if(restaurant.getMenu()[i].getType()==VEG)
            cout<<"VEG";
        if(restaurant.getMenu()[i].getType()==SPC)
            cout<<"SPC";
        if(restaurant.getMenu()[i].getType()==BVG)
            cout<<"BVG";
        cout<<" ";
        cout<<restaurant.getMenu()[i].getPrice();
        cout<<"NIS"<<endl;
    }
    BaseAction :: complete();
}

std::string PrintMenu::toString() const {
    string status="";

    if(getStatus()==ERROR)
        status=" Error";
    if(getStatus()==PENDING)
        status=" Pending";
    if(getStatus()==COMPLETED)
        status=" Completed";
    return "menu"+status;
}

PrintMenu *PrintMenu::clone() {
    return new PrintMenu(*this);
}



PrintTableStatus::PrintTableStatus(int id) : BaseAction(),tableId(id) {}

void PrintTableStatus::act(Restaurant &restaurant)
{
    cout<<"Table "; cout<<tableId;
    cout<<" status: ";
    if(restaurant.getTable(tableId)->isOpen())
    {
        cout << "open" << endl;
        cout << "Customers:" << endl;
        for(unsigned int i=0;i<restaurant.getTable(tableId)->getCustomers().size();i++)
        {
            cout<<restaurant.getTable(tableId)->getCustomers()[i]->getId();
            cout<<" ";
            cout<<restaurant.getTable(tableId)->getCustomers()[i]->getName()<<endl;
        }
        cout << "Orders:" << endl;
        for(unsigned int i=0;i<restaurant.getTable(tableId)->getOrders().size();i++)
        {
            cout<<restaurant.getTable(tableId)->getOrders()[i].second.getName()+" ";
            cout<<restaurant.getTable(tableId)->getOrders()[i].second.getPrice();
            cout<<"NIS ";
            cout<<restaurant.getTable(tableId)->getOrders()[i].first<<endl;
        }
        cout<<"Current Bill: ";
        cout<<restaurant.getTable(tableId)->getBill();
        cout<<"NIS"<<endl;
    }
    else  cout<<"closed"<<endl;

    BaseAction :: complete();
}


std::string PrintTableStatus::toString() const {
    string returnString="";
    string status="";

    if(getStatus()==ERROR)
        status=" Error";
    if(getStatus()==PENDING)
        status=" Pending";
    if(getStatus()==COMPLETED)
        status=" Completed";

    returnString+="status "+to_string(tableId)+status;
    return returnString;
}


PrintTableStatus *PrintTableStatus::clone() {
    return new PrintTableStatus(*this);
}







PrintActionsLog::PrintActionsLog() :BaseAction(){}

//function return action logs
void PrintActionsLog::act(Restaurant &restaurant) {
    for (unsigned int i=0; i < restaurant.getActionsLog().size(); i++) {
        cout << restaurant.getActionsLog()[i]->toString()<<endl;
    }
    BaseAction :: complete();
}

std::string PrintActionsLog::toString() const {
    return "";
}

PrintActionsLog *PrintActionsLog::clone() {
    return new PrintActionsLog(*this);
}






BackupRestaurant::BackupRestaurant() :BaseAction(){}

void BackupRestaurant::act(Restaurant &restaurant)
{
    if(backup==nullptr)
        backup=new Restaurant(restaurant);
    else
       *backup=restaurant;


    BaseAction :: complete();
}



std::string BackupRestaurant::toString() const {
    string returnString="";
    string status="";
    if(getStatus()==ERROR)
        status=" Error";
    if(getStatus()==PENDING)
        status=" Pending";
    if(getStatus()==COMPLETED)
        status=" Completed";

    returnString+="backup"+status;
    return returnString ;
}

BackupRestaurant *BackupRestaurant::clone() {
    return new BackupRestaurant(*this);
}

RestoreResturant::RestoreResturant() :BaseAction(){}

void RestoreResturant::act(Restaurant &restaurant) {
    if(backup == nullptr){
        BaseAction::error("No backup available");
        cout<<"Error: No backup available"<<endl;
    }
    else {
        restaurant = *backup;
        complete();
    }
}

std::string RestoreResturant::toString() const {
    string returnString="";
    string status="";
    if(getStatus()==ERROR)
        status=" Error: No backup available";
    if(getStatus()==PENDING)
        status=" Pending";
    if(getStatus()==COMPLETED)
        status=" Completed";

    returnString+="restore"+status;
    return returnString ;
}

RestoreResturant *RestoreResturant::clone() {
    return new RestoreResturant(*this);
}

BackupRestaurant::~BackupRestaurant() {

}

RestoreResturant::~RestoreResturant() {

}

Order::~Order() {

}

MoveCustomer::~MoveCustomer() {

}

Close::~Close() {

}

CloseAll::~CloseAll() {

}

PrintMenu::~PrintMenu() {

}

PrintTableStatus::~PrintTableStatus() {

}

PrintActionsLog::~PrintActionsLog() {

}
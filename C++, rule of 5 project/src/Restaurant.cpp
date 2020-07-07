#include "Restaurant.h"
#include <sstream>
#include <vector>
#include <string>
#include "Dish.h"
#include "Table.h"
#include "Action.h"
#include <iostream>
#include<fstream>
using namespace std;

//empty constructor
Restaurant::Restaurant() : open(false),tables(),menu(),actionsLog(),dishId(0),customerId(0) {}


//function get input and create a restaurant by parameters
Restaurant::Restaurant(const std::string &configFilePath) : open(false),tables(),menu(),actionsLog(),dishId(0),customerId(0){
    ifstream readFile;
    readFile.open(configFilePath); //reading file from input address
 //   cout<<readFile.is_open()<<endl;
    vector<int>capacities;

    string theLine;
    bool numOfTableFound = false; // when we found first number that indiciate number of table it will be true
    bool organziedTables = false; // indicate if restaurant opened tables
//    cout<<readFile.is_open()<<endl;
//    cout<<"checking preEntrence2"<<endl;
    if (readFile.is_open()) {
 //       cout<<"checking preEntrence1`"<<endl;
        while (!readFile.eof()) {
            getline(readFile, theLine); //saving the line we read right now in theLine
 //           cout<<"checking preEntrence"<<endl;
            if ((theLine != "") && (theLine !="\r") && (theLine!="\n") && (theLine.at(0) != '#') && (theLine.at(0)!=' ')) //if line is not empty and not starting with #
            {

//              cout<<"checking enterence"<<endl;
                if (!numOfTableFound) {
                    numOfTableFound = true; //we found the first number now
                    //numberOfTables = stoi(theLine); // updating number of tables, conver to int
                } else if (!organziedTables) // if restaurant know number of tables but didnt know capacity
                {
                    organziedTables = true;
                    openTheTables(theLine);
                } else // making dishes for menu
                {
                    makeDish(theLine);
                }
            }
        }

        readFile.close();
    }
}



//get number of table
int Restaurant::getNumOfTables() const {
    return (unsigned int)tables.size();
}

//function return table (pointer)
Table* Restaurant::getTable(int ind) {
    if((unsigned int)ind<tables.size())
        return tables[ind];
    return nullptr;
}

//function opens tables and give them capacity our addition
void Restaurant::openTheTables(string &capacityLine) {
    int index=0;
    stringstream streamLine;
    streamLine.str(capacityLine);
    while (getline(streamLine, capacityLine, ',')) {
        index=stoi(capacityLine);
        Table *table = new Table(index);
        tables.push_back(table);
    }
}

//function create dish with id
void Restaurant::makeDish(string &dishLine) {
    int price;
    string type;
    string name;

    stringstream streamLine;
    streamLine.str(dishLine);

    getline(streamLine, dishLine, ',');
    name=dishLine;
    getline(streamLine, dishLine, ',');
    type=dishLine;
    getline(streamLine, dishLine, ',');
    price=stoi(dishLine);
    DishType i;
    if(type=="VEG")
        i=VEG;
    if(type=="ALC")
        i=ALC;
    if(type=="BVG")
        i=BVG;
    if(type=="SPC")
        i=SPC;
    Dish dish(dishId,name,price,i);
    menu.push_back(dish);
    dishId++;
}

// function return menu of restaurant
std::vector<Dish> &Restaurant::getMenu() {
    return menu;
}

//Get address of Vector of pointers of action log
const std::vector<BaseAction*>& Restaurant::getActionsLog() const
{
    return actionsLog;
}

//get dish by id
Dish Restaurant::getDish(int id) {
    int indx=0;
    if(menu[indx].getId()==id)
        return menu[0];

    for(unsigned int i=0;i<menu.size();i++)
    {
        if(menu[i].getId()==id)
            indx=i;
    }
    return menu[indx];
}

void Restaurant::start() {
    this->open = true;
    std::cout << "Restaurant is now open!" << std::endl;
    string customerName;
    string customerType;
    vector<Customer *> customersList;
    bool isCloseAll=false;
    while (isCloseAll==false)
    {

        customersList.clear();
        stringstream action;
        string input;
        getline(cin, input);
        action.str(input);
        getline(action, input, ' ');

        if (input == "open")
        {
            getline(action, input, ' ');
            int j=stoi(input);
            getline(action, input, ',');
            while(!((input=="alc")|(input=="spc")|(input=="chp")|(input=="veg")))
            {
                customerName=input;
                getline(action, input, ' ');
                customerType=input;
                if(customerType=="veg") {
                    VegetarianCustomer *customer = new VegetarianCustomer(customerName, customerId);
                    customersList.push_back(customer);
                }
                if(customerType=="alc"){
                    AlchoholicCustomer *customer=new AlchoholicCustomer(customerName,customerId);
                    customersList.push_back(customer);
                }
                if(customerType=="chp"){
                    CheapCustomer *customer=new CheapCustomer(customerName,customerId);
                    customersList.push_back(customer);
                }
                if(customerType=="spc"){
                    SpicyCustomer *customer=new SpicyCustomer(customerName,customerId);
                    customersList.push_back(customer);
                }
                customerId++;
                getline(action, input, ',');
            }
            OpenTable *openTable=new OpenTable(j,customersList);
            actionsLog.push_back(openTable);
            openTable->act(*this);
        }

        else if (input == "order")
        {
            getline(action, input, ' ');
            int j=stoi(input);
            Order *order=new Order(j);
            actionsLog.push_back(order);
            order->act(*this);
        }
        else if (input == "move")
        {
            getline(action, input, ' ');
            int src=stoi(input);
            getline(action, input, ' ');
            int dst=stoi(input);
            getline(action, input, ' ');
            int cId=stoi(input);
            MoveCustomer *moveCustomer=new MoveCustomer(src,dst,cId);
            actionsLog.push_back(moveCustomer);
            moveCustomer->act(*this);
        }

        else if (input == "close")
        {
            getline(action, input, ' ');
            int Tid=stoi(input);
            Close *close=new Close(Tid);
            close->act(*this);
            actionsLog.push_back(close);
        }

        else if (input == "closeall")
        {
            isCloseAll=true;
            CloseAll *closeAll=new CloseAll();
            actionsLog.push_back(closeAll);
            closeAll->act(*this);
        }

        else if (input == "menu")
        {
            PrintMenu *printMenu=new PrintMenu();
            actionsLog.push_back(printMenu);
            printMenu->act(*this);
        }

        else if (input == "status")
        {
            getline(action, input, ' ');
            int i=stoi(input);
            PrintTableStatus *printTableStatus=new PrintTableStatus(i);
            actionsLog.push_back(printTableStatus);
            printTableStatus->act(*this);
        }

        else if (input == "log")
        {
            PrintActionsLog *printActionsLog=new PrintActionsLog();
            printActionsLog->act(*this);
            delete(printActionsLog);
        }

        else if (input == "backup")
        {
            BackupRestaurant *backupRestaurant=new BackupRestaurant();
            backupRestaurant->act(*this);
            actionsLog.push_back(backupRestaurant);
        }

        else if (input == "restore")
        {
            RestoreResturant *restoreResturant=new RestoreResturant();
            restoreResturant->act(*this);
            actionsLog.push_back(restoreResturant);
        }
    }

}

//restaurant destructor
Restaurant::~Restaurant() {
    menu.clear();
    for(auto table:tables)
        delete(table);
    tables.clear();

    for(auto action:actionsLog)
        delete(action);

    actionsLog.clear();
}

//copy constructor
Restaurant::Restaurant(const Restaurant &restaurant):open(restaurant.open),tables(),menu(),actionsLog(),dishId(restaurant.dishId),customerId(restaurant.customerId)
{
    for(unsigned int i=0;i<restaurant.menu.size();i++)
        this->menu.push_back(restaurant.menu[i]);

    for(unsigned int i=0;i<restaurant.tables.size();i++)
        this->tables.push_back(new Table(*restaurant.tables[i]));

    for(unsigned int i=0;i<restaurant.actionsLog.size();i++)
        this->actionsLog.push_back(restaurant.actionsLog[i]->clone());
}


//Move assignment operator
Restaurant &Restaurant::operator=(Restaurant &&other)
{
    if(this!=&other)
    {
        tables.clear();
        actionsLog.clear();
        menu.clear();
        delete(this);
        *this=other;
    }
    return *this;
}

//Copy assignment operator
Restaurant &Restaurant::operator=(const Restaurant &other)
{
    if(this==&other)
        return *this;

    for(unsigned int i=0;i<tables.size();i++)
        delete(tables[i]);

    this->tables.clear();


    for(unsigned int i=0;i<other.tables.size();i++)
        this->tables.push_back(new Table(*other.tables[i]));

    for(auto action:actionsLog)
        delete(action);

    actionsLog.clear();

    for(unsigned int i=0;i<other.actionsLog.size();i++)
    {
        this->actionsLog.push_back(other.actionsLog[i]->clone());
    }

    for(unsigned int i=0;i<other.menu.size();i++)
        this->menu.push_back(other.menu[i]);

    dishId=other.dishId;
    customerId=other.customerId;
    open=other.open;
    return *this;
}

//move constructor
Restaurant::Restaurant(Restaurant &&other)
{
    *this=other;
    delete(&other);
}
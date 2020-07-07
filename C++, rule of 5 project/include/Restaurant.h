#ifndef RESTAURANT_H_
#define RESTAURANT_H_
#include <vector>
#include <string>
#include "Dish.h"
#include "Table.h"
#include "Customer.h"
#include "Action.h"

class Restaurant {
public:

    Restaurant(); //V
    Restaurant(const std::string &configFilePath); //V
    void start(); //V
    int getNumOfTables() const; //V
    Table* getTable(int ind); //V
    const std::vector<BaseAction*>& getActionsLog() const;
    std::vector<Dish>& getMenu();

    void openTheTables(string &capacityLine);//V
    void makeDish(string &dishLine); //v
    Dish getDish(int id); //V
    ~Restaurant(); //V
    Restaurant(const Restaurant &restaurant);//copy constructor
    Restaurant& operator=(Restaurant&& other);//move assignment operator
    Restaurant& operator=(const Restaurant& other);//copy assignment operator
    Restaurant(Restaurant&& other);//move constructor
private:
    bool open;
    std::vector<Table*> tables;
    std::vector<Dish> menu;
    std::vector<BaseAction*> actionsLog;
    int dishId;
    int customerId;
};

#endif
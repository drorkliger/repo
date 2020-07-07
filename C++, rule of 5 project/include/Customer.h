#ifndef CUSTOMER_H_
#define CUSTOMER_H_

#include <vector>
#include <string>
#include "Dish.h"

using namespace std;


class Customer {
public:
    Customer(std::string c_name, int c_id); //v
    virtual std::vector<int> order(const std::vector<Dish> &menu) = 0; //-
    virtual std::string toString() const = 0; //-
    std::string getName() const; //v
    int getId() const; //v
    virtual Customer* clone()=0;//
    virtual ~Customer(); //destructor
    virtual string getType()=0;
private:
    const std::string name;
    const int id;
};


class VegetarianCustomer : public Customer {
public:
    VegetarianCustomer(std::string name, int id); //v
    std::vector<int> order(const std::vector<Dish> &menu);//v
    std::string toString() const;
    VegetarianCustomer* clone();
    string getType();
    //~VegetarianCustomer(); //ther is no need for a destructor 17/11 17:10
private:

};


class CheapCustomer : public Customer {
public:
    CheapCustomer(std::string name, int id);
    std::vector<int> order(const std::vector<Dish> &menu);
    std::string toString() const;
    //~CheapCustomer(); //there is no need for destructor 17/11 17:36
    CheapCustomer(const CheapCustomer &customer);
    bool isAlready() const;
    CheapCustomer* clone();
    string getType();

private:
    bool already;
};


class SpicyCustomer : public Customer {
public:
    SpicyCustomer(std::string name, int id);
    std::vector<int> order(const std::vector<Dish> &menu);
    std::string toString() const;
    //~SpicyCustomer(); //ther is no need for constructor 17/11 19:38
    SpicyCustomer(const SpicyCustomer &customer);
    bool isOrdered() const;
    SpicyCustomer* clone();
    string getType();
private:
    bool hasOrdered;
};


class AlchoholicCustomer : public Customer {
public:
    AlchoholicCustomer(std::string name, int id);
    std::vector<int> order(const std::vector<Dish> &menu);
    std::string toString() const;
    vector <Dish> onlyALCMenu(const vector<Dish> &menu);
    vector <Dish> removeCheapestALC(const vector<Dish> &alchoholBVG,int index);
    //~AlchoholicCustomer(); //17/11 18:32
    AlchoholicCustomer(const AlchoholicCustomer &customer);
    bool isMenu() const;
    AlchoholicCustomer* clone();
    string getType();
private:
    vector <Dish> alchoholicBVG;
    bool isMenuExist;
};

#endif
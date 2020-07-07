#include "Dish.h"
#include <iostream>
#include <vector>

using namespace std;

//constructor
Dish::Dish(int d_id, string d_name, int d_price, DishType d_type): id(d_id), name(d_name) , price(d_price), type(d_type) {}


//id of a dish
int Dish::getId() const
{
    return id;
}

//function return name of dish
string Dish:: getName() const
{
    return name;
}

// function return price of dish
int Dish:: getPrice() const
{
    return price;
}

//function return type of dish
DishType Dish:: getType() const
{
    return type;
}



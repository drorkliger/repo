#ifndef DISH_H_
#define DISH_H_

#include <string>

enum DishType {
    VEG, SPC, BVG, ALC
};

class Dish {
public:
    Dish(int d_id, std::string d_name, int d_price, DishType d_type); //V
    int getId() const;//V
    std::string getName() const;//V
    int getPrice() const;//v
    DishType getType() const;//v
    //~Dish();  //there is no need for a destructor 17/11 16:54

private:
    const int id;
    const std::string name;
    const int price;
    const DishType type;
};


#endif
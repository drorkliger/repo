#include <iostream>
#include "Dish.h"
#include "Customer.h"
#include <vector>
using namespace std;


//////////////Customer//////////////

//constructor
Customer::Customer(string c_name, int c_id) : name(c_name),id(c_id){}

//get name
string Customer::getName() const {return name;}

//function return id
int Customer::getId() const {return id;}

//destructor of customer
//Customer::~Customer() {} //tehre is no need for a destructor for costumer 17/11 16:59

//copy constructor of customer
//Customer::Customer(const Customer &c):name(c.getName()),id(c.getId()){}



//////////////vegetrian customer////////////

//constructor
VegetarianCustomer::VegetarianCustomer(std::string name, int id) : Customer(name, id){}

//VegetarianCustomer::VegetarianCustomer(const VegetarianCustomer &customer) : VegetarianCustomer(customer.getName(),customer.getId()) {}

//order
std::vector<int> VegetarianCustomer::order(const std::vector<Dish> &menu) {

    std::vector<int> v; //vector that contain order dishes id

    if(menu.size()==0)
        return v;

    int firstVEG = -1; //will hold the id of first vegeterian dish
    int drinkInd = -1; //will hold the id of most expancive BVG
    int drinkPri = -1; //will hold price of most expancive BVG
    int i = 0;
    bool found = false; //indicate if there is a vegeterian dish


    while ((unsigned int)i< menu.size() && found==false)
    {
        if (menu[i].getType() == VEG)
        {
            found = true;
            firstVEG = menu[i].getId();
        }
        i++;
    }

    //18/11 3:27
    //find veg dish with smallest id
    if(found==true)
    {
        for(unsigned int i=0;i<menu.size();i++)
        {
            if((menu[i].getType() == VEG) & (menu[i].getId()<firstVEG))
                firstVEG=menu[i].getId();
        }
    }

    //finding most expancive BVG
    if(found) {
        for (unsigned i = 0; i < menu.size(); i++) {
            if ((menu[i].getType() == BVG) & (menu[i].getPrice() > drinkPri)) {
                drinkInd = menu[i].getId();
                drinkPri = menu[i].getPrice();
            }
        }
    }

    // checking if we found a proper product for ordering
    if (firstVEG != -1)
        v.push_back(firstVEG);
    if (drinkInd != -1)
        v.push_back(drinkInd);


    return v;
}

string VegetarianCustomer::getType() {
    return "veg";
}

//toString
std::string VegetarianCustomer::toString() const {
    return this->getName()+"with id "+to_string(this->getId())+" orderd vegetrian dish";

}

//destructor of vegeterian customer
//VegetarianCustomer::~VegetarianCustomer() {}





///////////cheap customer////////////


//constructor
CheapCustomer::CheapCustomer(std::string name, int id) : Customer(name, id),already(false) {}


//Copy Constructor
CheapCustomer::CheapCustomer(const CheapCustomer &customer) : CheapCustomer(customer.getName(),customer.getId())
{
    already=customer.isAlready();
}


//order for cheap customer return dish id
std::vector<int> CheapCustomer::order(const std::vector<Dish> &menu)
{
    int cheapDish; //id of cheapest dish
    int cheapPrice;//price of cheapest dish
    vector <int> v; //vector of orders

    if(menu.size()==0)
        return v;

    if (this->isAlready()==false) { //if first order of costumer

        already=true; // cheap customer is ordering


        if (menu.size() > 0) { //menu is not empty
            //assuming first dish is the cheapiest
            cheapPrice = menu[0].getPrice();
            cheapDish = menu[0].getId();
        }
        else // if there are no dish in menu
            return v;

        for (unsigned int i = 1;i < menu.size();i++) //finding cheapest dish
        {
            if (menu[i].getPrice() < cheapPrice)
            {
                cheapDish = menu[i].getId();
                cheapPrice = menu[i].getPrice();
            }
        }
        v.push_back(cheapDish);
    }
    return v;
}


//get costumer already ordered
bool CheapCustomer::isAlready() const {
    return already;
}

std::string CheapCustomer::toString() const
{
    return this->getName()+"with id "+to_string(this->getId())+" ordered cheap order";
}
//destructor of cheapcustomer
//CheapCustomer::~CheapCustomer() {} // there is no need for destructor

string CheapCustomer::getType() {
    return "chp";
}



//////////// spicy customer ///////////

//constructor
SpicyCustomer::SpicyCustomer(std::string name, int id) : Customer(name, id),hasOrdered(false){}

//copyConstructor of spicyCustomer
SpicyCustomer::SpicyCustomer(const SpicyCustomer &customer) :SpicyCustomer(customer.getName(),customer.getId())
{
    hasOrdered=isOrdered();
}


std::vector<int> SpicyCustomer::order(const std::vector<Dish> &menu) {

    std::vector<int> v; // the vector we return
    if(menu.size()==0)
        return v;
    int dishInd = 0; //index of checked dish
    int dishPri = 0; // price of checked dish
    bool isFound=false; //indicate if we there is any bvg, until not found assuming not
    if (!hasOrdered) // to know  if liik for a SPC or BVG
    {
        for (unsigned int i = 0;i < menu.size();i++)
        {
            if ((menu[i].getType() == SPC) & (menu[i].getPrice() > dishPri)) //if we found more expancive SPC dish
            {
                dishInd = i;
                dishPri = menu[i].getPrice();
            }
        }


        if (menu[dishInd].getType() == SPC) // we assumed dish num 0 is most expancsive dish, we checked to avoid mistake
        {
            v.push_back(menu[dishInd].getId());
            hasOrdered=true;

        }
    }
    else {
        int indexOfDish=-1; // index of cheapeast BVG of the  menu
        for (unsigned int i = 0;i < menu.size();i++)
        {
            if(isFound) {
                if ((menu[i].getType() == BVG) & (menu[i].getPrice() < dishPri)) {
                    dishInd = menu[i].getId();
                    dishPri = menu[i].getPrice();
                    indexOfDish=i;
                }
            }
            if((menu[i].getType() == BVG) & (!isFound)) {
                dishPri = menu[i].getPrice();
                dishInd = menu[i].getId();
                indexOfDish=i;
                isFound=true;
            }

        }
        if (menu[indexOfDish].getType() == BVG) // it can be that the most expansive SPC dish is dish number 0 and therfore we check that
        {
            v.push_back(menu[indexOfDish].getId());
        }
    }

    return v;
}

string SpicyCustomer::getType() {
    return "spc";
}


//return if has previous orders
bool SpicyCustomer::isOrdered() const {
    return hasOrdered;
}

string SpicyCustomer::toString() const {
    return this->getName()+" with id "+ to_string(this->getId())+" ordered";
}





///////////////// alcoholic customer /////////////////

//constructor
AlchoholicCustomer::AlchoholicCustomer(string name, int id) : Customer(name, id),alchoholicBVG(), isMenuExist(false) {}

//copy constructor
AlchoholicCustomer::AlchoholicCustomer(const AlchoholicCustomer &customer):AlchoholicCustomer(customer.getName(),customer.getId())
{
    for (unsigned int i=0;i<customer.alchoholicBVG.size();i++)
    {
        this->alchoholicBVG.push_back(customer.alchoholicBVG[i]);
    }
}

//is menu exist
bool AlchoholicCustomer::isMenu() const {
    return isMenuExist;
}

//making order return vector of alc's id
vector<int> AlchoholicCustomer::order(const vector<Dish> &menu) {
    vector <int> v; // the vector we return
    if(menu.size()==0)
        return v;
    int cheapInd; //index of current cheapest in ALC menu
    int tempPri; //hold the cheapest price

    if(!isMenuExist)  //if we didnt make a ALC menu until now
        alchoholicBVG = onlyALCMenu(menu);



    if(alchoholicBVG.size()!=0) //there is at least one drink
    {
        //assuming the first drink in the menu is the cheapest
        cheapInd=0;
        tempPri=alchoholicBVG[0].getPrice();

        for(unsigned int i=1;i<alchoholicBVG.size();i++)
        {
            if(alchoholicBVG[i].getPrice()<tempPri)
            {
                cheapInd=i;
                tempPri=alchoholicBVG[i].getPrice();
            }
                // if prices are equal but have different id
            else if((alchoholicBVG[i].getPrice()==tempPri) & (alchoholicBVG[i].getId() < alchoholicBVG[cheapInd].getId()))
            {
                cheapInd=i;
                tempPri=alchoholicBVG[i].getPrice();
            }
        }
        v.push_back(alchoholicBVG[cheapInd].getId());
        alchoholicBVG=removeCheapestALC(alchoholicBVG,cheapInd);//for next time
    }
    return v;
}


//function remove ALC bevrage from alcoholic BVG menu
vector <Dish> AlchoholicCustomer ::removeCheapestALC(const vector<Dish> &alchoholBVG,int index)
{
    vector <Dish> v;
    for(unsigned int i=0;i<alchoholBVG.size();i++)
    {
        if(i!=(unsigned) index)
            v.push_back(alchoholBVG[i]);
    }
    return v;
}


//function create a ALC menu
vector <Dish> AlchoholicCustomer ::onlyALCMenu(const vector<Dish> &menu){
    vector <Dish> v;
    for(unsigned int i=0;i<menu.size();i++)
    {
        if(menu[i].getType()==ALC)
            v.push_back(menu[i]);
    }
    isMenuExist=true; //we created a ALC menu
    return v;
}

string AlchoholicCustomer::toString() const {
    return this->getName()+" with id "+to_string(this->getId())+" ordered alcohol";
}

//destructor of alcholic customer
//AlchoholicCustomer::~AlchoholicCustomer() //there is no need
//{
//    alchoholicBVG.clear();
//}

AlchoholicCustomer *AlchoholicCustomer::clone()
{
    vector <Dish> cloneAlc;
    for(unsigned int i=0;i<this->alchoholicBVG.size();i++)
        cloneAlc.push_back(alchoholicBVG[i]);

    AlchoholicCustomer *alchoholicCustomer=new AlchoholicCustomer(this->getName(),this->getId());
    alchoholicCustomer->isMenuExist=this->isMenuExist;
    return alchoholicCustomer;


}

string AlchoholicCustomer::getType() {
    return "alc";
}


Customer::~Customer() {

}



VegetarianCustomer *VegetarianCustomer::clone() {
    return new VegetarianCustomer(*this);
}

CheapCustomer *CheapCustomer::clone() {
    return new CheapCustomer(*this);
}

SpicyCustomer *SpicyCustomer::clone() {
    return new SpicyCustomer(*this);
}
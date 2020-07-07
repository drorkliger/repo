=========================================================================================
	 	     C++ Restaurant managment by Dror Kliger
=========================================================================================

In this assignment I wrote a C++ program that simulates a restaurant management
system. The program will open the restaurant, assign customers to tables, make orders,
provide bills to the tables, and other requests.
The program gets a config file as an input, which includes all required information about
the restaurant opening - number of tables, number of available seats in each table, and 
details about the dishes in the menu.
There are 4 types of customers in this restaurant, each customer type has its own way of
ordering from the menu. An order may be taken from a table more than once, in such cases 
some customers may order a different dish.
Each table in the restaurant has a limited amount of seats available (this info is provided in the
config file). 
The restaurant can’t connect tables together, nor accommodates more customers
than the number of seats available in a table. 
In this restaurant, it’s impossible to add new customers to an open table, but it’s 
possible to move customers from one table to another.
A bill of a table is the total price of all dishes ordered for that table.

=========================================================================================
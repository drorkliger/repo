package bgu.spl.mics.application.passiveObjects;

import java.io.Serializable;

/**
 * Passive data-object representing a information about a certain book in the inventory.
 * You must not alter any of the given public methods of this class. 
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class BookInventoryInfo implements Serializable {

	private String bookTitle; // book name
	private int amount; // number of books
	private int price; // price of book

//	private int soldBooksAmount;
//	private int totalBooksAmount;


	public BookInventoryInfo (String name, int amount, int price)
	{
		bookTitle=name;
		this.amount=amount;
		this.price=price;

//		soldBooksAmount=0;
//		totalBooksAmount=amount;
	}

	/**
     * Retrieves the title of this book.
     * <p>
     * @return The title of this book.   
     */
	public String getBookTitle()

	{
		return bookTitle;
	}

	/**
     * Retrieves the amount of books of this type in the inventory.
     * <p>
     * @return amount of available books.      
     */
	public int getAmountInInventory()

	{
		return amount;
	}

	/**
     * Retrieves the price for  book.
     * <p>
     * @return the price of the book.
     */
	public int getPrice()

	{
		return price;
	}

	//function reduce amount of this book after selling one
	public void reduceAmount()

	{
		amount--;
	}
}

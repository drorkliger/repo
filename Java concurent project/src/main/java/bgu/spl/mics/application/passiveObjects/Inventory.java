package bgu.spl.mics.application.passiveObjects;


import java.io.*;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive data-object representing the store inventory.
 * It holds a collection of {@link BookInventoryInfo} for all the
 * books in the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class Inventory implements Serializable {

	//private LinkedBlockingQueue<BookInventoryInfo> collection;
	//private ConcurrentHashMap <Integer,LinkedBlockingQueue<String>> collection;

	private ConcurrentHashMap<String,BookInventoryInfo> collection;
	private HashMap <String,Integer> allBooks;

    enum option{
		NOT_IN_STOCK, SUCCESSFULLY_TAKEN
	}


	// singleton part 1 of 3
private static class SingletonHolder
	{
		private static final Inventory instance= new Inventory();
	}

	//singleton part 2 of 3
	private Inventory()
	{
		collection=new ConcurrentHashMap<>();
		allBooks=new HashMap<>();
	}
	/**
     * Retrieves the single instance of this class.
     */
	//singleton part 3 of 3
	public static Inventory getInstance() {

		return SingletonHolder.instance;
	}




	
	/**
     * Initializes the store inventory. This method adds all the items given to the store
     * inventory.
     * <p>
     * @param inventory 	Data structure containing all data necessary for initialization
     * 						of the inventory.
     */
	public void load (BookInventoryInfo [] inventory )
	{//adding the books to the inventory
		for(BookInventoryInfo bookInventoryInfo:inventory)
			collection.putIfAbsent(bookInventoryInfo.getBookTitle(),bookInventoryInfo);
	}
	



	/**
     * Attempts to take one book from the store.
     * <p>
     * @param book 		Name of the book to take from the store
     * @return 	an {@link Enum} with options NOT_IN_STOCK and SUCCESSFULLY_TAKEN.
     * 			The first should not change the state of the inventory while the 
     * 			second should reduce by one the number of books of the desired type.
     */
	public OrderResult take (String book) {

		if(checkAvailabiltyAndGetPrice(book)>-1) {
			collection.get(book).reduceAmount();
			return OrderResult.valueOf("SUCCESSFULLY_TAKEN");
			}
		return  OrderResult.valueOf("NOT_IN_STOCK");
	}
	




	/**
     * Checks if a certain book is available in the inventory.
     * <p>
     * @param book 		Name of the book.
     * @return the price of the book if it is available, -1 otherwise.
     */
	public int checkAvailabiltyAndGetPrice(String book) {

		if(collection.get(book)!=null)
			return collection.get(book).getPrice();

		return -1;
	}




	/**
     * 
     * <p>
     * Prints to a file name @filename a serialized object HashMap<String,Integer> which is a Map of all the books in the inventory. The keys of the Map (type {@link String})
     * should be the titles of the books while the values (type {@link Integer}) should be
     * their respective available amount in the inventory. 
     * This method is called by the main method in order to generate the output.
     */
	public void printInventoryToFile(String filename){
		for(BookInventoryInfo bookInventoryInfo:collection.values())
			allBooks.put(bookInventoryInfo.getBookTitle(),bookInventoryInfo.getAmountInInventory());

		try {
			FileOutputStream fileOutputStream=new FileOutputStream(filename);
			ObjectOutputStream objectOutputStream=new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(allBooks);
			objectOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

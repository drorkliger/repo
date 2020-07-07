package bgu.spl.mics.application.passiveObjects;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing the store finance management. 
 * It should hold a list of receipts issued by the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class MoneyRegister implements Serializable {

	private int earnings;
	private BlockingQueue<OrderReceipt> ListOfReceipt;



    //singleton part 1 of 3
	private static class SingletonHolder
	{
		private static final MoneyRegister moneyRegister = new MoneyRegister();
	}

	//singleton part 2 of 3
	private MoneyRegister()
	{
		earnings=0;
		ListOfReceipt=new LinkedBlockingQueue<OrderReceipt>();
	}

	//singleton part 3 of 3
	/**
	 * Retrieves the single instance of this class.
	 */
	public static MoneyRegister getInstance()
	{
		return SingletonHolder.moneyRegister;
	}
	








	/**
     * Saves an order receipt in the money register.
     * <p>   
     * @param r		The receipt to save in the money register.
     */
	public void file (OrderReceipt r)
	{
			ListOfReceipt.add(r);
	}









	/**
     * Retrieves the current total earnings of the store.  
     */
	public int getTotalEarnings()
	{
		return earnings;
	}

	public void UpdatingEarning(int amount)
	{
		this.earnings=earnings+amount;
	}











	/**
     * Charges the credit card of the customer a certain amount of money.
     * <p>
     * @param amount 	amount to charge
     */
	public void chargeCreditCard(Customer c, int amount)
	{
		if(c.getAvailableCreditAmount()>=amount) {
			c.charge(amount);
			UpdatingEarning(amount);
		}
	}










	/**
     * Prints to a file named @filename a serialized object List<OrderReceipt> which holds all the order receipts 
     * currently in the MoneyRegister
     * This method is called by the main method in order to generate the output.. 
     */
	public void printOrderReceipts(String filename) {
		LinkedList<OrderReceipt>orderReceipts=new LinkedList<>();
		for(OrderReceipt orderReceipt:ListOfReceipt)
			orderReceipts.add(orderReceipt);

		try {
			FileOutputStream fileOutputStream=new FileOutputStream(filename);
			ObjectOutputStream objectOutputStream=new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(orderReceipts);
			objectOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

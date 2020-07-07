package bgu.spl.mics.application.passiveObjects;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Passive data-object representing a customer of the store.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class Customer implements Serializable {

	private int id;
	private String name;
	private String address;
	private int distance;
	private List <OrderReceipt> Receipts;
    //private Object lock;
    private int creditCardNumber;
    private int creditCardAmount;


    //constructor 27/11 20:44

    public Customer (int id, String name, String address, int distance, int creditCardNumber, int creditCardAmount)
	{
		this.id=id;
		this.name = name;
		this.address=address;
		this.distance=distance;
		this.Receipts=new LinkedList<OrderReceipt>();
		this.creditCardNumber=creditCardNumber;
		this.creditCardAmount=creditCardAmount;
		//this.lock=new Object();
	}


	public void addRecipt(OrderReceipt orderRecipt) {
		Receipts.add(orderRecipt);
	}

	/**
     * Retrieves the name of the customer.
     */
	// 27/11 20:46
	public String getName()
	{
		return name;
	}





	/**
     * Retrieves the ID of the customer  . 
     */
	// 27/11 20:48
	public int getId()
	{
		return id;
	}
	





	/**
     * Retrieves the address of the customer.  
     */
	// 27/11 20:48
	public String getAddress()
	{
		return address;
	}








	/**
     * Retrieves the distance of the customer from the store.  
     */
	// 27/11 20:49
	public int getDistance()
	{
		return distance;
	}








	/**
     * Retrieves a list of receipts for the purchases this customer has made.
     * <p>
     * @return A list of receipts.
     */
	// 27/7 20:49
	public List<OrderReceipt> getCustomerReceiptList()
	{
		return Receipts;
	}
	








	/**
     * Retrieves the amount of money left on this customers credit card.
     * <p>
     * @return Amount of money left.   
     */
	// 27/11 20:51
	public int getAvailableCreditAmount()
	{
		return creditCardAmount;
	}
	





	/**
     * Retrieves this customers credit card serial number.    
     */
	public int getCreditNumber()
	{
		return creditCardNumber;
	}



    //function charge card of a customer
	public void charge (int amount)
	{

		if(amount<=this.creditCardAmount)
		{
			this.creditCardAmount=creditCardAmount-amount;
		}
	}

	/*public Object getLock() {
		return lock;
	}*/
}

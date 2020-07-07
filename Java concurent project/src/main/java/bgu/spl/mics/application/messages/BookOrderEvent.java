package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;

public class BookOrderEvent implements Event<OrderReceipt>
{
    private String bookName;
    private int orderTick;
    //private String serviceName;
    //private int Tick;
    //private int Tick;
    Customer customer;
    public BookOrderEvent(Customer customer, String bookName, int orderTick)
    {
        this.customer=customer;
        this.bookName=bookName;
        this.orderTick=orderTick;
    }


    public String getBookName() {
        return bookName;
    }

    public int getOrderTick() {
        return orderTick;
    }

    public Customer getCustomer() {
        return customer;
    }

}

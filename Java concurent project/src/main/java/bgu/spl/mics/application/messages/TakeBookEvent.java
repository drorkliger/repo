package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.OrderResult;

public class TakeBookEvent implements Event<OrderResult>
{
    private String bookName;
    private int orderTick;
    public TakeBookEvent(String bookName)
    {
        this.bookName=bookName;
        this.orderTick=orderTick;
    }

    public String getBookName() {
        return bookName;
    }

}

package bgu.spl.mics.application.messages;


import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.BookInventoryInfo;

import java.util.concurrent.atomic.AtomicBoolean;

public class CheckAvailabilityAndGetPriceEvent implements Event<Integer> {

    private String bookName;

    public CheckAvailabilityAndGetPriceEvent(String bookName)
    {
        this.bookName=bookName;
    }


    public String getBookName()
    {
        return bookName;
    }
}

package bgu.spl.mics.application.messages;

import java.awt.*;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;

public class DeliveryEvent implements Event<DeliveryVehicle> {

    private String bookName;
    private String adress;
    private int distance;

    public DeliveryEvent(String bookName,String address,int distance)
    {
        this.adress=address;
        this.bookName=bookName;
        this.distance=distance;
    }

    public String getBookName() {
        return bookName;
    }

    public String getAdress() {
        return adress;
    }

    public int getDistance() {
        return distance;
    }
}

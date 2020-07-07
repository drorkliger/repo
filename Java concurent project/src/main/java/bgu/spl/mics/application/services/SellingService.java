package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.*;

import static bgu.spl.mics.application.passiveObjects.OrderResult.*;


/**
 * Selling service in charge of taking orders from customers.
 * Holds a reference to the {@link MoneyRegister} singleton of the store.
 * Handles {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class SellingService extends MicroService{
	private MoneyRegister moneyRegister;
	private int currentTick;
	private int lastTick;

	public SellingService(String name) {
		super(name);
		moneyRegister=MoneyRegister.getInstance();
		currentTick=0;
		lastTick=0;
	}



	@Override
	protected void initialize() {
		// the code from here is susbcribe broadcast of tick
		subscribeBroadcast(TickBroadcast.class, br->{
			currentTick=br.getCurrentTick();
			lastTick=br.getLastTick();
			if(lastTick==currentTick)//check if its the last tick
			{
				terminate();
			}
		});






// the code from here is subscribe of book order event
		subscribeEvent(BookOrderEvent.class, ev->{
			Future <OrderResult>futureTakeBook=null;
			Future <Integer> futureCheckAv=null;
			OrderResult orderResult=null;
			OrderReceipt orderReceipt=null;
			Integer price=-1;
			int orderId=(int)(Math.random()*1000);
			CheckAvailabilityAndGetPriceEvent checkAvailabilityEvent=new CheckAvailabilityAndGetPriceEvent(ev.getBookName());


			futureCheckAv=sendEvent(checkAvailabilityEvent);//send event to check whether the book is available
			if(futureCheckAv!=null)
				price=futureCheckAv.get();

			if((int)price>-1 && ev.getCustomer().getAvailableCreditAmount()>=price) {//if the book is available try to take it

				TakeBookEvent takeBookEvent = new TakeBookEvent(ev.getBookName());
				synchronized (ev.getCustomer()) {
					if(ev.getCustomer().getAvailableCreditAmount()>=price) {
						futureTakeBook=sendEvent(takeBookEvent);
						if(futureTakeBook!=null)
							orderResult = futureTakeBook.get();

						if (orderResult == SUCCESSFULLY_TAKEN) {//checks if the book was taken successfully

							orderReceipt = new OrderReceipt(orderId, getName(), ev.getCustomer().getId(), ev.getBookName(), price, currentTick, ev.getOrderTick(), currentTick);
							moneyRegister.chargeCreditCard(ev.getCustomer(), price);//charge the customer
							ev.getCustomer().addRecipt(orderReceipt);//adds the recipt to the customers recipt list
							moneyRegister.file(orderReceipt);

							DeliveryEvent deliveryEvent = new DeliveryEvent(ev.getBookName(), ev.getCustomer().getAddress(), ev.getCustomer().getDistance());
							sendEvent(deliveryEvent);//sends the book to delivery
						}
					}
				}
			}

				complete(ev, orderReceipt);

			});
		ThreadCounter.getInstance().increaseCounter();//update the the initializer that it finished initializing
}
}


















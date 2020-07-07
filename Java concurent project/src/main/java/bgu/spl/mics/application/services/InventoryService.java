package bgu.spl.mics.application.services;

//import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CheckAvailabilityAndGetPriceEvent;
import bgu.spl.mics.application.messages.TakeBookEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;

//import bgu.spl.mics.application.messages.TakeBookEvent;

/**
 * InventoryService is in charge of the book inventory and stock.
 * Holds a reference to the {@link Inventory} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class InventoryService extends MicroService{

	private Inventory inventory;
	private int currentTick;
	private int lastTick;


	public InventoryService(String name) {
		super(name);//the service name
		inventory=Inventory.getInstance();
		currentTick=0;
		lastTick=0;

	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, br->{
			currentTick=br.getCurrentTick();
			lastTick=br.getLastTick();
			if(lastTick==currentTick)//check if its the last tick
			{
				terminate();
			}
		});

		subscribeEvent(CheckAvailabilityAndGetPriceEvent.class,ev-> {//check whether the book is available and return the price
			Integer price=inventory.checkAvailabiltyAndGetPrice(ev.getBookName());
			complete(ev,price);
		});

		subscribeEvent(TakeBookEvent.class, ev->{//takes a book from the inventory
			OrderResult orderResult;
			orderResult=inventory.take(ev.getBookName());
			complete(ev,orderResult);
		});

		ThreadCounter.getInstance().increaseCounter();//update the the initializer that it finished initializing
	}

}

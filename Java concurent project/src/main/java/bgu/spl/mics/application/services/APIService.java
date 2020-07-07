package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BookOrderEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;
import javafx.util.Pair;

//import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * APIService is in charge of the connection between a client and the store.
 * It informs the store about desired purchases using {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class APIService extends MicroService{

	private Customer customer;
	private int currentTick;
	private String name;//service name
	private int lastTick;


	private ConcurrentHashMap <Integer,LinkedBlockingQueue<String>> tickOrderHash=new ConcurrentHashMap<>();//saves to every tick, the books we need to order

	//the pair<> represent book name and tick
	public APIService(LinkedBlockingQueue<Pair<String,Integer>> orderSchedule,Customer customer,String name) {
		super(name);
		this.name=name;//inserts the service name
		this.customer=customer;
		this.currentTick=0;
		for(Pair<String,Integer> pair:orderSchedule)
		{
			if(pair.getValue()<=lastTick) {
				tickOrderHash.putIfAbsent(pair.getValue(), new LinkedBlockingQueue<String>());
				try {
					tickOrderHash.get(pair.getValue()).put(pair.getKey());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void initialize()
	{
		//gets the ticks and checks if its the last one
		subscribeBroadcast(TickBroadcast.class,br-> {
			currentTick = br.getCurrentTick();
			lastTick = br.getLastTick();
			if (currentTick == lastTick) {
				terminate();
			} else {

				while (tickOrderHash.get(currentTick) != null && !tickOrderHash.get(currentTick).isEmpty())//checks if there are book to order in this tick
				{
					BookOrderEvent bookOrderEvent = new BookOrderEvent(customer, tickOrderHash.get(currentTick).poll(), currentTick);
					sendEvent(bookOrderEvent);
				}
			}
		});

		ThreadCounter.getInstance().increaseCounter();//update the the initializer that it finished initializing
	}



}


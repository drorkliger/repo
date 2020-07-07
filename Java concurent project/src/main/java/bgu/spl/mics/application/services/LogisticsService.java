package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AcquireVehicleEvent;
import bgu.spl.mics.application.messages.DeliveryEvent;
import bgu.spl.mics.application.messages.ReleaseVehicleEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;

/**
 * Logistic service in charge of delivering books that have been purchased to customers.
 * Handles {@link DeliveryEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LogisticsService extends MicroService {

	private int currentTick;
	private int lastTick;

	public LogisticsService(String name) {
		super(name);
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
		subscribeEvent(DeliveryEvent.class,ev->{
			Future <Future<DeliveryVehicle>>future=null;
			Future<DeliveryVehicle> vehicleFuture=null;
			DeliveryVehicle deliveryVehicle=null;
			AcquireVehicleEvent acquireVehicleEvent=new AcquireVehicleEvent();//try to acquire a vehicle
			future=sendEvent(acquireVehicleEvent);
			if(future!=null)
				vehicleFuture=future.get();
			complete(acquireVehicleEvent,vehicleFuture);

			if(vehicleFuture!=null)
				deliveryVehicle=vehicleFuture.get();
			ReleaseVehicleEvent releaseVehicleEvent=new ReleaseVehicleEvent(deliveryVehicle);
			sendEvent(releaseVehicleEvent);
			complete(ev,deliveryVehicle);
		});
		ThreadCounter.getInstance().increaseCounter();//update the the initializer that it finished initializing
	}

}

package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AcquireVehicleEvent;
import bgu.spl.mics.application.messages.ReleaseVehicleEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link ResourceHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ResourceService extends MicroService{

	private ResourcesHolder resourcesHolder;
	private int currentTick;
	private int lastTick;
	private BlockingQueue <Future<DeliveryVehicle>> givenVehicles;


	public ResourceService(String name) {
		super(name);
		resourcesHolder=ResourcesHolder.getInstance();
		currentTick=0;
		lastTick=0;
		givenVehicles=new LinkedBlockingQueue<>();
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, br->{
			currentTick=br.getCurrentTick();
			lastTick=br.getLastTick();
			Future<DeliveryVehicle> deliveryVehicleFuture=null;

			if(lastTick==currentTick)//check if its the last tick
			{
				if(!givenVehicles.isEmpty())
				{
					for(Future future:givenVehicles)
					{//checks if there are futures that not resolved after terminate
						deliveryVehicleFuture=givenVehicles.poll();
						if(!deliveryVehicleFuture.isDone())//if not resolved, get resolved with null
							deliveryVehicleFuture.resolve(null);
					}
				}
				terminate();
			}
		});

		subscribeEvent(AcquireVehicleEvent.class,ev->{
			Future<DeliveryVehicle> future=resourcesHolder.acquireVehicle();
			if(!future.isDone()) {
				try {
					givenVehicles.put(future);//if the future wasn't resolved, it is added to the waiting queue
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			complete(ev,future);//resolve the resolved or not resolved future
		});

		subscribeEvent(ReleaseVehicleEvent.class,ev->{
			resourcesHolder.releaseVehicle(ev.getDeliveryVehicle());//send a request to release the vehicle
		});
		ThreadCounter.getInstance().increaseCounter();//update the the initializer that it finished initializing
	}
}
























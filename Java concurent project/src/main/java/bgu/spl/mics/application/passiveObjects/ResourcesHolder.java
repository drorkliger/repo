package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing the resource manager.
 * You must not alter any of the given public methods of this class.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class ResourcesHolder implements Serializable {

	//these queues represent the status of vehicles
	private BlockingQueue<DeliveryVehicle> available;//available vehicles
	private BlockingQueue<DeliveryVehicle> taken;//taken vehicles
	private BlockingQueue <Future<DeliveryVehicle>> given;//given future



	//singleton part 1 of 3

	private static class SingletonHolder
	{
		private static final ResourcesHolder resourceHolder = new ResourcesHolder();
	}

	//singleton part 2 of 3
	private ResourcesHolder ()
	{
		available= new LinkedBlockingQueue<DeliveryVehicle>();
		taken=new LinkedBlockingQueue<DeliveryVehicle>();
		given=new LinkedBlockingQueue<>();
	}

		//singleton part 3 of 3
	/**
	 * Retrieves the single instance of this class.
	 */
	public static ResourcesHolder getInstance()
	{
		return SingletonHolder.resourceHolder;
	}




	/**
     * Tries to acquire a vehicle and gives a future object which will
     * resolve to a vehicle.
     * <p>
     * @return 	{@link Future<DeliveryVehicle>} object which will resolve to a 
     * 			{@link DeliveryVehicle} when completed.   
     */
	public Future<DeliveryVehicle> acquireVehicle() {
		DeliveryVehicle toResolve=null;
		Future<DeliveryVehicle> f= new Future<>();
		if(!available.isEmpty()) {//if there is a free car, allocate it to the asker
			toResolve = available.poll();
			taken.add(toResolve);//resolve the vehicle to the future
			f.resolve(toResolve);
		}
		else {
			try {
				given.put(f);//adds the future to the waiting queue
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return f;
	}






	
	/**
     * Releases a specified vehicle, opening it again for the possibility of
     * acquisition.
     * <p>
     * @param vehicle	{@link DeliveryVehicle} to be released.
     */
	public void releaseVehicle(DeliveryVehicle vehicle) {
		if(!given.isEmpty()) {
			given.poll().resolve(vehicle);//if there is someone who waits, he has the first priority to take

		}else {
				taken.remove(vehicle);
				available.add(vehicle);
			}
		}


	
	/**
     * Receives a collection of vehicles and stores them.
     * <p>
     * @param vehicles	Array of {@link DeliveryVehicle} instances to store.
     */
	public void load(DeliveryVehicle[] vehicles)

	{
		for (DeliveryVehicle v:vehicles)
			available.add(v);//loads all the vehicle to the available queue
	}

}

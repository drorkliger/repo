package bgu.spl.mics.application.services;

//import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

import java.util.Timer;
//import java.util.TimerTask;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link Tick Broadcast}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService {

	private Timer timer;
	private int speed; //represent how much miliseconds for a tick
	private int duration; //indicate the duration of the system
	private int tickCounter;//currentTick


	public TimeService(int speed, int duration) {

		super("Timer");
		this.speed = speed;
		this.duration = duration;
		tickCounter=1; // because instructions
		//timer = new Timer(); //?
		//initialize(); //probably should insert to the book manager runner
	}

	@Override
	protected void initialize() {
		while (tickCounter <duration) {
			sendBroadcast(new TickBroadcast(tickCounter,duration));//send ticks to everyservice
			try {
				Thread.sleep((long) speed);//waits speed amount of time until next tick
			} catch (Exception e) {
			}
			tickCounter++;
		}
		sendBroadcast(new TickBroadcast(tickCounter,duration));//send the last tick
		terminate();
	}
}

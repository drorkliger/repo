package bgu.spl.mics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	// this hash map contains message queue for each micro service in the system, search
	// specific message queue by specific micro service
	ConcurrentHashMap <MicroService,LinkedBlockingQueue<Message>> map;
	//this hash map has type of event as indexes, the purpose is to have a queue of micro services that subscribed to
	// specific event type
	ConcurrentHashMap <Class<? extends Event>,LinkedBlockingQueue<MicroService>> mapSubscribersOfEvents;

	//this hash map has type of broadcast as indexes, the purpose is to have a queue of micro services that subscribed to
	// specific broadcast type
	ConcurrentHashMap <Class<? extends Broadcast>,LinkedBlockingQueue<MicroService>> mapSubscribersOfBroadcasts;

	//hash map that contains future objects
	ConcurrentHashMap <Event,Future> mapOfFutersInProcess;


	private static class SingletonHolder {
		private static final MessageBusImpl messageBusImpl = new MessageBusImpl();
	}

	public static MessageBusImpl getInstance() {
		return SingletonHolder.messageBusImpl;
	}

	private MessageBusImpl() {
		map	=	new ConcurrentHashMap<>();
		mapSubscribersOfEvents	=	new ConcurrentHashMap <>();
		mapSubscribersOfBroadcasts= new ConcurrentHashMap <>();
		mapOfFutersInProcess =	new ConcurrentHashMap<>();
	}


	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		mapSubscribersOfEvents.putIfAbsent(type, new LinkedBlockingQueue<>());
		try {
			mapSubscribersOfEvents.get(type).put(m);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		mapSubscribersOfBroadcasts.putIfAbsent(type, new LinkedBlockingQueue<>());
		try {
			mapSubscribersOfBroadcasts.get(type).put(m);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		mapOfFutersInProcess.get(e).resolve(result);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		if(mapSubscribersOfBroadcasts.get(b.getClass()) != null)
				for(MicroService microService: mapSubscribersOfBroadcasts.get(b.getClass())) {
					try {
						map.get(microService).put(b);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Future<T> f = null;
		MicroService microService1 = null;
		try {
			if (mapSubscribersOfEvents.get(e.getClass()) != null) {
				synchronized (mapSubscribersOfEvents.get(e.getClass())) {
				//synchronized (map.get(mapSubscribersOfEvents.get(e.getClass()).peek())){
					if (!mapSubscribersOfEvents.get(e.getClass()).isEmpty()) {
						microService1 = mapSubscribersOfEvents.get(e.getClass()).take();
						f = new Future<>();
						mapOfFutersInProcess.put(e, f);
						map.get(microService1).put(e);
						mapSubscribersOfEvents.get(e.getClass()).put(microService1);
					}
				}
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		return f;
	}






	@Override
	public void register(MicroService m) {
		map.putIfAbsent(m, new LinkedBlockingQueue<>());
	}

	@Override
	public void unregister(MicroService m) {

		for(Message message:map.get(m))
		{
			Future future=mapOfFutersInProcess.get(message);
			future.resolve(null);
		}
		for(LinkedBlockingQueue<MicroService> linkedBlockingQueue:mapSubscribersOfEvents.values())
		{
			synchronized (linkedBlockingQueue) {
				linkedBlockingQueue.remove(m);
			}
		}

		for(LinkedBlockingQueue<MicroService> linkedBlockingQueue:mapSubscribersOfBroadcasts.values())
				linkedBlockingQueue.remove(m);



		map.remove(m);
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		return (Message)((BlockingQueue)map.get(m)).take();
	}



}
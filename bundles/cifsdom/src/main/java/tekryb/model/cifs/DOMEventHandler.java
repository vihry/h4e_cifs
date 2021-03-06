package tekryb.model.cifs;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.events.Event;

public abstract class DOMEventHandler implements Runnable {

	Event _event;
	
	Runnable _runnable;

	final Lock _lock = new ReentrantLock();
	
	public DOMEventHandler() {
		
	}
	
	public void setEvent( Event val ) {
		_event = val;
	}
	
	public Event getEvent( ) {
		return _event;
	}
	
	public void setRunnable( Runnable val ) {
		_runnable = val;
	}
	
	public Runnable getRunnable( ) {
		return _runnable;
	}
	
	public Lock getLock( ) {
		return _lock;
	}
	
	//public void run();
//	{
//		
//		try {
//			_lock.lock();
//			
//		} finally {
//			_lock.lock();	
//		}
//		
//	}

}

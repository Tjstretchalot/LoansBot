package me.timothy.bots;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Meant to be used in anonymous classes, this
 * retries {@code runImpl} by means of exponential back-off
 * 
 * @author Timothy
 *
 * @param <T> what is returned
 */
public abstract class Retryable<T> {
	private Logger logger;
	private String name;
	
	public Retryable(String name) {
		this.name = name;
		
		logger = LogManager.getLogger();
	}
	
	/**
	 * Runs runImpl at most 10 times or until the result is non-null.
	 * 
	 * @return runImpl's non-null result upon success, null on failure
	 */
	public T run() {
		int duration = 10000, times = 0;
		T result;
		while((result = runImpl()) == null) {
			times++;
			long sleepTime = (long) (duration * Math.pow(2, times));
			
			logger.debug(name + " failed (#" + times + "); retrying in " + sleepTime);
			try {
				Thread.sleep(sleepTime);
			}catch(InterruptedException ex) {
				throw new RuntimeException(ex);
			}
		}
		return result;
	}
	
	protected abstract T runImpl();
}

package me.timothy.bots;

import me.timothy.bots.responses.AppliedFormattableObject;
import me.timothy.bots.responses.LoanFormattableObject;
import me.timothy.bots.responses.ResponseInfoFactory;

/**
 * Contains utility functions for initializing the response info factory
 * 
 * @author Timothy
 */
public class LoansResponseInfoFactory {

	/**
	 * Initializes the response info factory with loans and applied 
	 * objects.
	 */
	public static void init() {
		for(int i = 1; i <= 3; i++) {
			ResponseInfoFactory.base.addLongtermObject("loans" + i, new LoanFormattableObject());
			ResponseInfoFactory.base.addLongtermObject("applied" + i, new AppliedFormattableObject());
		}
	}

}

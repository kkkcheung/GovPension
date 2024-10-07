package uk.gov.dwp.uc.pairtest.exception;

public class InvalidPurchaseException extends RuntimeException {
	
	/**
	 * 
	 * @param message
	 */
	public InvalidPurchaseException(String message) {
		super(message);
	}

}

package me.timothy.bots.responses;

public class MoneyFormattableObject implements FormattableObject {
	private int amount;
	
	public MoneyFormattableObject(int am) {
		amount = am;
	}
	
	public int getAmount() {
		return amount;
	}
	
	@Override
	public String toFormattedString(String myName, ResponseInfo info) {
		return Integer.toString(amount);
	}

}

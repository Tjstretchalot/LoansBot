package me.timothy.bots.responses;

import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;

public class MoneyFormattableObject implements FormattableObject {
	private int amount;
	
	public MoneyFormattableObject(int am) {
		amount = am;
	}
	
	public int getAmount() {
		return amount;
	}
	
	@Override
	public String toFormattedString(ResponseInfo info, String myName, FileConfiguration config, LoansDatabase db) {
		return Integer.toString(amount);
	}
}

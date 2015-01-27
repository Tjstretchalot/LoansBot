package me.timothy.tests.bots.responses;

import static org.junit.Assert.*;

import me.timothy.bots.responses.MoneyFormattableObject;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;

import org.junit.Test;

public class TestResponseInfoFactory {
	@Test
	public void test() {
		String format = "$loan <user1> <money1>";
		
		String[] usersToTest = new String[] { "/u/username", "/u/user", "user" };
		String[] usersToTestExpected = new String[] { "username", "user", "user" };
		String[] moniesToTest = new String[] { "10", "$10", "$1.00", "$.50", "$985.43", "10$", "11.23$" };
		int[] moniesToTestExpected = new int[] { 1000, 1000, 100, 50, 98543, 1000, 1123 };
		
		for(int userInd = 0; userInd < usersToTest.length; userInd++) {
			for(int moneyInd = 0; moneyInd < moniesToTest.length; moneyInd++) {
				String message = "$loan " + usersToTest[userInd] + " " + moniesToTest[moneyInd];
				
				ResponseInfo respInfo = ResponseInfoFactory.getResponseInfo(format, message);
				
				assertEquals(usersToTestExpected[userInd], respInfo.getObject("user1").toString());
				assertEquals(moniesToTestExpected[moneyInd], ((MoneyFormattableObject) respInfo.getObject("money1")).getAmount());
			}
		}
	}

}

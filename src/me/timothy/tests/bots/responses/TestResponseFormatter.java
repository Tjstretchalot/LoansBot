package me.timothy.tests.bots.responses;

import junit.framework.Assert;
import me.timothy.bots.responses.MoneyFormattableObject;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;

import org.junit.Test;

public class TestResponseFormatter {

	@Test
	public void testCanFormat() {
		
		ResponseInfo respInfo = ResponseInfoFactory.getResponseInfo("$confirm <user1> <money1>", "$confirm /u/jeffenatrix 156.66");
		
		Assert.assertEquals("jeffenatrix", respInfo.getObject("user1").toString());
		Assert.assertEquals(15666, ((MoneyFormattableObject) respInfo.getObject("money1")).getAmount());
		ResponseFormatter respFormatter = new ResponseFormatter("hello <user1> you lent <money1>", respInfo);
		
		String response = respFormatter.getFormattedResponse(null, null);
		Assert.assertEquals("hello jeffenatrix you lent $156.66", response);
	}

}

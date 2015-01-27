package me.timothy.tests.bots.responses;

import junit.framework.Assert;
import me.timothy.bots.responses.GenericFormattableObject;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;

import org.junit.Test;

public class TestResponseFormatter {

	@Test
	public void testCanFormat() {
		ResponseInfo respInfo = new ResponseInfo();
		respInfo.addLongtermObject("author", new GenericFormattableObject("Timothy")); // 6, 7
		respInfo.addTemporaryObject("temp", new GenericFormattableObject("temporary")); // 4, 9
		// first ind = 9
		final String format = "Temp is <temp>; author is <author>; see! (by <author>)";
		final String expResult = "Temp is temporary; author is Timothy; see! (by Timothy)";
		
		ResponseFormatter respFormatter = new ResponseFormatter(format, respInfo);
		
		String response = respFormatter.getFormattedResponse(null, null);
		Assert.assertEquals(expResult, response);
	}

}

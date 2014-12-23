package ru.ydn.wicket.wicketorientdb.rest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletInputStream;

import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.request.Url;
import org.junit.Test;

import ru.ydn.wicket.wicketorientdb.AbstractTestClass;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class TestRestApi extends AbstractTestClass
{
	public static final String TEST_REST_CLASS="TestRest";
	private static final Pattern RID_PATTERN = Pattern.compile("#(\\d+:\\d+)");
	private static final Random RANDOM = new Random();
	
	@Test
	public void testGetDocument() throws Exception
	{
		ODocument doc = (ODocument) getDatabase().browseClass(TEST_REST_CLASS).current();
		ORID id = doc.getIdentity();
		String ret = executeUrl("orientdb/document/db/"+id.getClusterId()+":"+id.getClusterPosition(), "GET", null);
		assertEquals(doc.toJSON(), ret);
	}
	
	@Test
	public void testPostDocument() throws Exception
	{
		long current = getDatabase().countClass(TEST_REST_CLASS);
		String content = "{\"@class\":\"TestRest\",\"a\":\"test2\",\"b\":11,\"c\":false}";
		executeUrl("orientdb/document/db/", "POST", content);
		assertEquals(current+1, getDatabase().countClass(TEST_REST_CLASS));
	}
	
	@Test
	public void testDeleteDocument() throws Exception
	{
		long current = getDatabase().countClass(TEST_REST_CLASS);
		String content = "{\"@class\":\"TestRest\",\"a\":\"todelete\",\"b\":11,\"c\":false}";
		String created = executeUrl("orientdb/document/db/", "POST", content);
		assertEquals(current+1, getDatabase().countClass(TEST_REST_CLASS));
		Matcher rid = RID_PATTERN.matcher(created);
		assertTrue(rid.find());
		executeUrl("orientdb/document/db/"+rid.group(1), "DELETE", content);
		assertEquals(current, getDatabase().countClass(TEST_REST_CLASS));
	}
	
	@Test
	public void testQueryAndUpdate() throws Exception
	{
		ODocument doc = (ODocument) getDatabase().browseClass(TEST_REST_CLASS).current();
		String ret = executeUrl("orientdb/query/db/sql/select+from+"+TEST_REST_CLASS, "GET", null);
		assertTrue(ret.contains(doc.toJSON()));
		
		int nextB = RANDOM.nextInt();
		ret = executeUrl("orientdb/command/db/sql", "POST", "update "+TEST_REST_CLASS+" set b = "+nextB);
		doc.reload();
		assertEquals(nextB, doc.field("b"));
	}
	
	private String executeUrl(String _url, final String method, final String content) throws Exception
	{
		MockHttpServletRequest request = new MockHttpServletRequest(getApp(), wicketTester.getHttpSession(), wicketTester.getServletContext())
		{
			{
				setMethod(method);
			}

			@Override
			public ServletInputStream getInputStream() throws IOException {
				if(content==null) return super.getInputStream();
				else
				{
					final StringReader sr = new StringReader(content);
					return new ServletInputStream() {
						@Override
						public int read() throws IOException {
							return sr.read();
						}
					};
				}
			}
			
		};
		
		Url url = Url.parse(_url, Charset.forName(request.getCharacterEncoding()));
		request.setUrl(url);
		request.setMethod(method);
		wicketTester.processRequest(request);
		return wicketTester.getLastResponseAsString();
	}
	
}
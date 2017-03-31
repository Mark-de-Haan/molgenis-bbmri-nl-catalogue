package org.molgenis.promise.client;

import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.xml.xpath.Jaxp13XPathTemplate;
import org.springframework.xml.xpath.XPathOperations;

import javax.xml.transform.Source;
import java.io.IOException;

public class PromiseDataUnmarshaller implements Unmarshaller
{

	private static final String DATA_CONTAINER_ELEMENT = "getDataForXMLResult";

	private final XPathOperations xpath = new Jaxp13XPathTemplate();

	@Override
	public boolean supports(Class<?> clazz)
	{
		return clazz.isAssignableFrom(String.class);
	}

	@Override
	public Object unmarshal(Source source) throws IOException, XmlMappingException
	{
		return xpath.evaluateAsString("//*[local-name()='" + DATA_CONTAINER_ELEMENT + "']/text()", source);
	}
}

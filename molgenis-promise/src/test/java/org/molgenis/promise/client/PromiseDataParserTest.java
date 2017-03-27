package org.molgenis.promise.client;

import org.mockito.*;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

import static com.google.common.collect.Lists.newArrayList;
import static org.testng.Assert.assertEquals;

public class PromiseDataParserTest
{
	@Mock
	private PromiseClient promiseClient;

	@Mock
	private Entity credentials;

	private PromiseDataParser parser;

	@Captor
	private ArgumentCaptor<Consumer<XMLStreamReader>> consumerArgumentCaptor;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		parser = new PromiseDataParser(promiseClient);
	}

	@Test
	public void testParse() throws IOException, XMLStreamException
	{
		List<Entity> entities = newArrayList();
		parser.parse(credentials, 0, entities::add);

		Mockito.verify(promiseClient)
				.getData(Mockito.eq(credentials), Mockito.eq("0"), consumerArgumentCaptor.capture());

		InputStream is = getClass().getResourceAsStream("/cva_biobank_response.xml");
		XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(is);
		consumerArgumentCaptor.getValue().accept(xmlStreamReader);

		MapEntity entity1 = new MapEntity();
		entity1.set("PSI_REG_ID", "1006");
		entity1.set("MATERIAL_TYPES", "DNA_UIT_BLOEDCELLEN,BLOEDPLASMA(EDTA),BLOEDSERUM");

		MapEntity entity2 = new MapEntity();
		entity2.set("PSI_REG_ID", "1042");
		entity2.set("MATERIAL_TYPES", "OORSMEER");

		assertEquals(entities, newArrayList(entity1, entity2));
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Username-Password combination not found<BR>Combinatie Gebruikersnaam-Wachtwoord niet gevonden")
	public void testErroneousParse() throws IOException, XMLStreamException
	{
		List<Entity> entities = newArrayList();
		parser.parse(credentials, 0, entities::add);

		Mockito.verify(promiseClient)
				.getData(Mockito.eq(credentials), Mockito.eq("0"), consumerArgumentCaptor.capture());

		InputStream is = getClass().getResourceAsStream("/erroneous_biobank_response.xml");
		XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(is);
		consumerArgumentCaptor.getValue().accept(xmlStreamReader);
	}
}

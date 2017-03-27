package org.molgenis.promise.mapper;

import com.google.common.collect.Maps;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.promise.mapper.RadboudBiobankMapper.*;
import static org.molgenis.promise.mapper.RadboudMapper.XML_ID;
import static org.molgenis.promise.mapper.RadboudMapper.XML_IDAA;
import static org.molgenis.promise.mapper.RadboudSampleMap.*;
import static org.molgenis.promise.model.BbmriNlCheatSheet.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class RadboudBiobankMapperTest
{
	private RadboudBiobankMapper radboudBiobankMapper;
	private RadboudSampleMap radboudSampleMap;
	private RadboudDiseaseMap radboudDiseaseMap;
	private Map<String, String> biobank;
	private DataService dataService;

	private final Entity NL_COUNTRY_ENTITY = mock(Entity.class);
	private final Entity JURISTIC_PERSON_83 = mock(Entity.class);
	private final Entity COLLECTION_OTHER = mock(Entity.class);
	private final Entity AGE_TYPE = mock(Entity.class);
	private final Entity RBB_BIOBANK = mock(Entity.class);
	private final Entity DISEASE_SPECIFIC = mock(Entity.class);
	private final EntityType PERSON_METADATA = mock(EntityType.class);
	private final EntityType SAMPLE_COLLECTIONS_METADATA = mock(EntityType.class);

	private ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);

	@BeforeMethod
	@SuppressWarnings("unchecked")
	public void beforeMethod()
	{
		dataService = mock(DataService.class);
		radboudSampleMap = mock(RadboudSampleMap.class);
		radboudDiseaseMap = mock(RadboudDiseaseMap.class);

		Stream<Entity> resultStream = mock(Stream.class);
		List<Entity> resultEntities = mock(List.class);
		when(resultStream.collect(toList())).thenReturn(resultEntities);
		when(dataService.findAll(any(String.class), any(Stream.class))).thenReturn(resultStream);

		when(dataService.findOneById(REF_COUNTRIES, "NL")).thenReturn(NL_COUNTRY_ENTITY);
		when(dataService.getEntityType(REF_PERSONS)).thenReturn(PERSON_METADATA);
		when(dataService.getEntityType(SAMPLE_COLLECTIONS_ENTITY)).thenReturn(SAMPLE_COLLECTIONS_METADATA);
		when(dataService.findOneById(REF_JURISTIC_PERSONS, "83")).thenReturn(JURISTIC_PERSON_83);
		when(dataService.findOneById(REF_COLLECTION_TYPES, "OTHER")).thenReturn(COLLECTION_OTHER);
		when(dataService.findOneById(REF_AGE_TYPES, "YEAR")).thenReturn(AGE_TYPE);
		when(dataService.findOneById(REF_BIOBANKS, "RBB")).thenReturn(RBB_BIOBANK);
		when(dataService.findOneById(REF_COLLECTION_TYPES, "DISEASE_SPECIFIC")).thenReturn(DISEASE_SPECIFIC);

		Attribute stringAttr = mock(Attribute.class);
		when(stringAttr.getDataType()).thenReturn(STRING);
		Attribute mrefAttr = mock(Attribute.class);
		when(mrefAttr.getDataType()).thenReturn(MREF);
		Attribute xrefAttr = mock(Attribute.class);
		when(xrefAttr.getDataType()).thenReturn(XREF);
		Attribute boolAttr = mock(Attribute.class);
		when(boolAttr.getDataType()).thenReturn(BOOL);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(ID)).thenReturn(stringAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(ACRONYM)).thenReturn(stringAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(PUBLICATIONS)).thenReturn(stringAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(BIOBANK_SAMPLE_ACCESS_URI)).thenReturn(stringAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(WEBSITE)).thenReturn(stringAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(BIOBANK_DATA_ACCESS_URI)).thenReturn(stringAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(PRINCIPAL_INVESTIGATORS)).thenReturn(mrefAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(INSTITUTES)).thenReturn(mrefAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(NAME)).thenReturn(stringAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(TYPE)).thenReturn(mrefAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(AGE_UNIT)).thenReturn(xrefAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(DESCRIPTION)).thenReturn(stringAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(CONTACT_PERSON)).thenReturn(mrefAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(BIOBANKS)).thenReturn(mrefAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(BIOBANK_SAMPLE_ACCESS_FEE)).thenReturn(boolAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(BIOBANK_SAMPLE_ACCESS_JOINT_PROJECTS)).thenReturn(boolAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(BIOBANK_SAMPLE_ACCESS_DESCRIPTION)).thenReturn(stringAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(BIOBANK_DATA_ACCESS_FEE)).thenReturn(boolAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(BIOBANK_DATA_ACCESS_JOINT_PROJECTS)).thenReturn(boolAttr);
		when(SAMPLE_COLLECTIONS_METADATA.getAttribute(BIOBANK_DATA_ACCESS_DESCRIPTION)).thenReturn(stringAttr);
		when(PERSON_METADATA.getAttribute(ID)).thenReturn(stringAttr);
		when(PERSON_METADATA.getAttribute(FIRST_NAME)).thenReturn(stringAttr);
		when(PERSON_METADATA.getAttribute(COUNTRY)).thenReturn(xrefAttr);

		radboudBiobankMapper = new RadboudBiobankMapper(dataService, mock(EntityManager.class));

		biobank = Maps.newHashMap();
		when(biobank.get(XML_ID)).thenReturn("9000");
		when(biobank.get(XML_IDAA)).thenReturn("1");
		when(biobank.get(XML_TITLE)).thenReturn("Inflammatoire Darmziekten");
		when(biobank.get(XML_DESCRIPTION)).thenReturn("Long description");
		when(biobank.get(XML_CONTACT_PERSON)).thenReturn("Dr. Abc de Fgh");
		when(biobank.get(XML_ADDRESS1)).thenReturn("afdeling Maag-Darm-Leverziekten");
		when(biobank.get(XML_ADDRESS2)).thenReturn("Post 123");
		when(biobank.get(XML_ZIP_CODE)).thenReturn("1234 AB");
		when(biobank.get(XML_LOCATION)).thenReturn("Poppenwier");
		when(biobank.get(XML_EMAIL)).thenReturn("abc@def.gh");
		when(biobank.get(XML_PHONE)).thenReturn("123-4567890");
		when(biobank.get(XML_TYPEBIOBANK)).thenReturn("1");
		when(biobank.get(XML_VOORGESCH)).thenReturn("1");
		when(biobank.get(XML_FAMANAM)).thenReturn("1");
		when(biobank.get(XML_BEHANDEL)).thenReturn("1");
		when(biobank.get(XML_FOLLOWUP)).thenReturn("2");
		when(biobank.get(XML_BEELDEN)).thenReturn("1");
		when(biobank.get(XML_VRAGENLIJST)).thenReturn("2");
		when(biobank.get(XML_OMICS)).thenReturn("2");
		when(biobank.get(XML_ROUTINEBEP)).thenReturn("2");
		when(biobank.get(XML_GWAS)).thenReturn("2");
		when(biobank.get(XML_HISTOPATH)).thenReturn("2");
		when(biobank.get(XML_OUTCOME)).thenReturn("2");
		when(biobank.get(XML_ANDERS)).thenReturn("2");
	}

	@Test
	public void mapNewBiobank()
	{
		Entity mappedEntity = radboudBiobankMapper.mapNewBiobank(biobank, radboudSampleMap, radboudDiseaseMap);

		assertEquals(mappedEntity.get(ACRONYM), null);
		assertEquals(mappedEntity.get(PUBLICATIONS), null);
		assertEquals(mappedEntity.get(BIOBANK_SAMPLE_ACCESS_URI), ACCESS_URI);
		assertEquals(mappedEntity.get(WEBSITE), "http://www.radboudbiobank.nl/");
		assertEquals(mappedEntity.get(BIOBANK_DATA_ACCESS_URI), ACCESS_URI);

		Iterator<Entity> investigatorIterator = mappedEntity.getEntities(PRINCIPAL_INVESTIGATORS).iterator();
		Entity investigator = investigatorIterator.next();
		assertEquals(investigator.get(ID), "9000_1");
		assertEquals(investigator.get(COUNTRY), NL_COUNTRY_ENTITY);
		assertFalse(investigatorIterator.hasNext());

		Iterator<Entity> instituteIterator = mappedEntity.getEntities(INSTITUTES).iterator();
		assertEquals(instituteIterator.next(), JURISTIC_PERSON_83);
		assertFalse(instituteIterator.hasNext());

		testDynamicMapping(mappedEntity);
		testFixedMapping(mappedEntity);
	}

	@Test
	public void mapExistingBiobank()
	{
		Entity existingCollection = new DynamicEntity(SAMPLE_COLLECTIONS_METADATA, dataService);

		String url = "http://abc.de/";

		existingCollection.set(ACRONYM, "ABC");
		existingCollection.set(PUBLICATIONS, "TEST");
		existingCollection.set(BIOBANK_SAMPLE_ACCESS_URI, url);
		existingCollection.set(WEBSITE, url);
		existingCollection.set(BIOBANK_DATA_ACCESS_URI, url);
		existingCollection.set(PRINCIPAL_INVESTIGATORS, emptyList());
		existingCollection.set(INSTITUTES, emptyList());

		Entity mappedEntity = radboudBiobankMapper
				.mapExistingBiobank(biobank, radboudSampleMap, radboudDiseaseMap, existingCollection);

		// check that these fields aren't overwritten
		assertEquals(mappedEntity.get(ACRONYM), "ABC");
		assertEquals(mappedEntity.get(PUBLICATIONS), "TEST");
		assertEquals(mappedEntity.get(BIOBANK_SAMPLE_ACCESS_URI), url);
		assertEquals(mappedEntity.get(WEBSITE), url);
		assertEquals(mappedEntity.get(BIOBANK_DATA_ACCESS_URI), url);
		assertEquals(mappedEntity.getEntities(PRINCIPAL_INVESTIGATORS), emptyList());
		assertEquals(mappedEntity.get(INSTITUTES), emptyList());

		testDynamicMapping(mappedEntity);
		testFixedMapping(mappedEntity);
	}

	/**
	 * Tests the portion of the mapping that is based on the input Radboud biobanks.
	 */
	private void testDynamicMapping(Entity mappedEntity)
	{
		assertEquals(mappedEntity.get(ID), "9000_1");
		assertEquals(mappedEntity.get(NAME), "Inflammatoire Darmziekten");

		Iterator<Entity> typeIterator = mappedEntity.getEntities(TYPE).iterator();
		assertEquals(typeIterator.next(), DISEASE_SPECIFIC);
		assertFalse(typeIterator.hasNext());

		verify(radboudSampleMap).getDataCategories(biobank);
		verify(radboudSampleMap).getMaterials("9000_1");
		verify(radboudSampleMap).getOmics("9000_1");
		verify(radboudSampleMap).getSex("9000_1");
		verify(radboudSampleMap).getAgeMin("9000_1");
		verify(radboudSampleMap).getAgeMax("9000_1");
		verify(radboudSampleMap).getSize("9000_1");

		verify(radboudDiseaseMap).getDiseaseTypes("1");
		assertEquals(mappedEntity.get(DESCRIPTION), "Long description");

		// verify new contact person entity is added
		verify(dataService, atLeastOnce()).findOne(eq(REF_PERSONS), any(String.class));
		verify(dataService, atLeastOnce()).add(eq(REF_PERSONS), entityCaptor.capture());
		Iterator<Entity> contactIterator = mappedEntity.getEntities(CONTACT_PERSON).iterator();
		Entity contactPerson = contactIterator.next();
		assertFalse(contactIterator.hasNext());
		assertEquals(contactPerson, entityCaptor.getValue());
		assertEquals(contactPerson.get(FIRST_NAME), "Dr. Abc de Fgh");
	}

	/**
	 * Tests the fixed portion of the mapping (hardcoded default values).
	 */
	private void testFixedMapping(Entity mappedEntity)
	{
		Iterator<Entity> biobankIterator = mappedEntity.getEntities(BIOBANKS).iterator();
		assertEquals(biobankIterator.next(), RBB_BIOBANK);
		assertFalse(biobankIterator.hasNext());

		assertEquals(mappedEntity.get(AGE_UNIT), AGE_TYPE);
		assertEquals(mappedEntity.get(BIOBANK_SAMPLE_ACCESS_FEE), true);
		assertEquals(mappedEntity.get(BIOBANK_SAMPLE_ACCESS_JOINT_PROJECTS), true);
		assertEquals(mappedEntity.get(BIOBANK_SAMPLE_ACCESS_DESCRIPTION), null);
		assertEquals(mappedEntity.get(BIOBANK_DATA_ACCESS_FEE), true);
		assertEquals(mappedEntity.get(BIOBANK_DATA_ACCESS_JOINT_PROJECTS), true);
		assertEquals(mappedEntity.get(BIOBANK_DATA_ACCESS_DESCRIPTION), null);
	}
}
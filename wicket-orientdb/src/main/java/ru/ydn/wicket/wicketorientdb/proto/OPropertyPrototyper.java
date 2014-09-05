package ru.ydn.wicket.wicketorientdb.proto;

import java.util.Arrays;
import java.util.List;

import ru.ydn.wicket.wicketorientdb.OrientDbWebSession;
import ru.ydn.wicket.wicketorientdb.proto.OClassPrototyper.OClassSetNameFix;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;

public class OPropertyPrototyper extends AbstractPrototyper<OProperty> {
	
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String LINKED_TYPE = "linkedType";
	public static final String LINKED_CLASS = "linkedClass";
	public static final String MANDATORY = "mandatory";
	public static final String READONLY = "readonly";
	public static final String NOT_NULL = "notNull";
	public static final String MIN = "min";
	public static final String MAX = "max";
	public static final String COLLATE = "collate";
	public static final List<String> OPROPERTY_ATTRS = Arrays.asList(NAME, TYPE, LINKED_TYPE, LINKED_CLASS, MANDATORY, READONLY, NOT_NULL, MIN, MAX, COLLATE);

	public static interface OPropertyFix
	{
		public OProperty setLinkedClass(OClass oClass);
		public OProperty setLinkedType(OType type);
	}
	
	private static final Class<?>[] FIX_INTERFACES = new Class<?>[]{OPropertyFix.class};
	
	private final String className;
	
	private OPropertyPrototyper(String className)
	{
		this.className = className;
	}

	@Override
	protected OProperty createInstance(OProperty proxy) {
		OSchema schema = OrientDbWebSession.get().getDatabase().getMetadata().getSchema();
		OClass oClass = schema.getClass(className);
		return oClass.createProperty(proxy.getName(), proxy.getType());
	}
	
	@Override
	protected Object handleGet(String propName, Class<?> returnType) {
		if("ownerClass".equals(propName))
		{
			OSchema schema = OrientDbWebSession.get().getDatabase().getMetadata().getSchema();
			return schema.getClass(className);
		}
		else return super.handleGet(propName, returnType);
	}

	@Override
	protected Class<OProperty> getMainInterface() {
		return OProperty.class;
	}
	
	@Override
	protected Class<?>[] getAdditionalInterfaces() {
		return FIX_INTERFACES;
	}
	
	public static OProperty newPrototype(String className)
	{
		return newPrototype(new OPropertyPrototyper(className));
	}

}

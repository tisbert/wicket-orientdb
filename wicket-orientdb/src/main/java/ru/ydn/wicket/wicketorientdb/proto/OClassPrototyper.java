package ru.ydn.wicket.wicketorientdb.proto;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.ydn.wicket.wicketorientdb.OrientDbWebSession;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OSchemaShared;
import com.orientechnologies.orient.core.metadata.schema.clusterselection.OClusterSelectionStrategy;

public class OClassPrototyper extends AbstractPrototyper<OClass> {
	
	public static final String NAME = "name";
	public static final String SHORT_NAME = "shortName";
	public static final String SUPER_CLASS = "superClass";
	public static final String OVER_SIZE = "overSize";
	public static final String STRICT_MODE = "strictMode";
	public static final String ABSTRACT = "abstract";
	public static final String CLUSTER_SELECTION = "clusterSelection";
	
	public static final List<String> OCLASS_ATTRS = Arrays.asList(NAME, SHORT_NAME, SUPER_CLASS, OVER_SIZE, STRICT_MODE, ABSTRACT, CLUSTER_SELECTION);
	
	public static interface OClassSetNameFix
	{
		public OClass setName(String iName);
	}
	
	private static final Class<?>[] FIX_INTERFACES = new Class<?>[]{OClassSetNameFix.class}; 
	

	private OClassPrototyper()
	{
		values.put("overSize", (float)0);
	}

	@Override
	protected OClass createInstance(OClass proxy) {
		OSchema schema = OrientDbWebSession.get().getDatabase().getMetadata().getSchema();
		return schema.createClass(proxy.getName());
	}

	@Override
	protected Class<OClass> getMainInterface() {
		return OClass.class;
	}
	
	public static OClass newPrototype()
	{
		return newPrototype(new OClassPrototyper());
	}
	
	
	

	@Override
	protected Class<?>[] getAdditionalInterfaces() {
		return FIX_INTERFACES;
	}
	
	

	@Override
	protected Object handleSet(String propName, Object value) {
		if("clusterSelection".equals(propName))
		{
			if(value instanceof OClusterSelectionStrategy)
			{
				return super.handleSet(propName, value);
			}
			else if(value instanceof CharSequence)
			{
				OSchema schema = OrientDbWebSession.get().getDatabase().getMetadata().getSchema();
				
				if(schema instanceof OSchemaShared)
				{
					return super.handleSet(propName, ((OSchemaShared)schema).getClusterSelectionFactory().newInstance(value.toString()));
				}
			}
		}
		//Default
		return super.handleSet(propName, value);
	}

	@Override
	protected Object handleCustom(Object proxy, Method method, Object[] args) {
		String methodName = method.getName();
		if("properties".equals(methodName) || "declaredProperties".equals(methodName))
		{
			return Collections.EMPTY_SET;
		}
		else
		{
			return super.handleCustom(proxy, method, args);
		}
	}

	@Override
	public String toString() {
		return "Prototype for '"+getMainInterface().getName()+"'";
	}
	
	
	
	

}
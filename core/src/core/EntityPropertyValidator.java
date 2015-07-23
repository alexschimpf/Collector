package core;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import entity.Entity;
import entity.EntityBodyDef;

public final class EntityPropertyValidator {

	private static final String CONFIG_FILENAME = "entity_properties.xml";
	
	private static EntityPropertyValidator instance;
	
	private final EntityProperties _globalProperties = new EntityProperties();
	private final HashMap<String, EntityProperties> _entityPropertiesMap = new HashMap<String, EntityProperties>();
	private final HashMap<String, String> _entityTypeClassMap = new HashMap<String, String>();
	
	public static EntityPropertyValidator getInstance() {
		if(instance == null) {
			instance = new EntityPropertyValidator();
		}
		
		return instance;
	}
	
	private EntityPropertyValidator() {
		try {
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(CONFIG_FILENAME));
			_buildModel(root);
		} catch(Exception e) {
			Gdx.app.error("collector", "EntityPropertyValidator", e);
		}
	}
	
	public Entity getEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		try {
			String type = (String)object.getProperties().get("type");
			String className = _entityTypeClassMap.get(type);
			Class<?> c = Class.forName(className);
			Constructor<?> constructor = c.getConstructor(EntityBodyDef.class, TextureMapObject.class, MapObject.class);
			Entity entity = (Entity)constructor.newInstance(bodyDef, object, bodySkeleton);
			entity.onPostCreate();
			
			return entity;
		} catch(Exception e) {
			Gdx.app.error("collector", "getEntity", e);
			return null;
		}
	}
	
	public void validateAndProcess(String entityType, MapProperties mapProperties) {
		if(!_entityPropertiesMap.containsKey(entityType)) {
			throw new NullPointerException("Entity type '" + entityType + "' is not supported");
		}
		
		EntityProperties entityProperties = _entityPropertiesMap.get(entityType);
		
		// Fill in override property values.
		for(Entry<String, String> override : entityProperties.OVERRIDE_MAP.entrySet()) {
			String name = override.getKey();
			String value = override.getValue();
			mapProperties.put(name, value);
		}		
		
		// Check to see if all required properties are set.
		Array<String> requiredPropertyNames = entityProperties.getRequiredPropertyNames();		
		requiredPropertyNames.addAll(_globalProperties.getRequiredPropertyNames());
		for(String requiredPropertyName : requiredPropertyNames) {
			if(!mapProperties.containsKey(requiredPropertyName)) {
				throw new NullPointerException("Entity of type '" + entityType + "' does not have required property " + requiredPropertyName);
			}
		}
		
		// Check to see if all used properties are valid.
		Iterator<String> iter = mapProperties.getKeys();
		while(iter.hasNext()) {
			String propertyName = iter.next();
			if(!_globalProperties.isPropertyNameValid(propertyName) && !entityProperties.isPropertyNameValid(propertyName)) {
				throw new NullPointerException("Entity of type '" + entityType + "' has invalid property " + propertyName);
			}
		}
		
		// Fill in missing optional properties with default values.
		Array<String> optionalPropertyNames = entityProperties.getOptionalPropertyNames();
		optionalPropertyNames.addAll(_globalProperties.getOptionalPropertyNames());
		for(String optionalPropertyName : optionalPropertyNames) {
			if(!mapProperties.containsKey(optionalPropertyName)) {
				EntityProperty property;
				if(_globalProperties.isPropertyNameValid(optionalPropertyName)) {
					property = _globalProperties.getProperty(optionalPropertyName);
				} else {
					property = entityProperties.getProperty(optionalPropertyName);
				}
				
				mapProperties.put(optionalPropertyName, property.defaultValue);
			}
		}
	}
	
	private void _buildModel(Element root) {
		_fillGlobalProperties(root.getChildByName("global_properties"));
		_fillEntityProperties(root.getChildByName("entity_properties"));
	}
	
	private void _fillGlobalProperties(Element globalPropertiesRoot) {
		Array<Element> requiredElems = globalPropertiesRoot.getChildByName("required").getChildrenByName("property");
		for(Element elem : requiredElems) {
			EntityProperty property = _buildRequiredPropertyFromXML(elem);
			_globalProperties.addProperty(property);
		}
		
		Array<Element> optionalElems = globalPropertiesRoot.getChildByName("optional").getChildrenByName("property");
		for(Element elem : optionalElems) {
			EntityProperty property = _buildOptionalPropertyFromXML(elem);
			_globalProperties.addProperty(property);
		}
	}
	
	private void _fillEntityProperties(Element entityPropertiesRoot) {
		Array<Element> entityElems = entityPropertiesRoot.getChildrenByName("entity");
		
		for(Element entityElem : entityElems) {
			String entityType = entityElem.get("type");
			String entityClass = entityElem.get("class");		
			_entityTypeClassMap.put(entityType, entityClass);
					
			EntityProperties properties = new EntityProperties();
			
			Element requiredChild = entityElem.getChildByName("required");
			if(requiredChild != null) {
				Array<Element> requiredElems = requiredChild.getChildrenByName("property");
				for(Element elem : requiredElems) {
					EntityProperty property = _buildRequiredPropertyFromXML(elem);
					properties.addProperty(property);
				}
			}
			
			Element optionalChild = entityElem.getChildByName("optional");
			if(optionalChild != null) {
				Array<Element> optionalElems = optionalChild.getChildrenByName("property");
				for(Element elem : optionalElems) {
					EntityProperty property = _buildOptionalPropertyFromXML(elem);
					properties.addProperty(property);
				}
				
			}
			
			Element overrideChild = entityElem.getChildByName("override");
			if(overrideChild != null) {
				Array<Element> overrideElems = overrideChild.getChildrenByName("property");
				for(Element elem : overrideElems) {
					String name = elem.get("name");
					String value = elem.get("value");
					properties.addOverride(name, value);
				}
			}

			_entityPropertiesMap.put(entityType, properties);
		}
	}
	
	private EntityProperty _buildRequiredPropertyFromXML(Element elem) {
		String name = elem.get("name");
		String note = elem.get("note", "");
		return new EntityProperty(name, true, null, note);
	}
	
	private EntityProperty _buildOptionalPropertyFromXML(Element elem) {
		String name = elem.get("name");
		String defaultValue = elem.get("default");
		String note = elem.get("note", "");
		return new EntityProperty(name, false, defaultValue, note);
	}
	
	public final class EntityProperties {
		
		public final HashMap<String, EntityProperty> REQUIRED_PROPERTY_MAP = new HashMap<String, EntityProperty>();
		public final HashMap<String, EntityProperty> OPTIONAL_PROPERTY_MAP = new HashMap<String, EntityProperty>();
		public final HashMap<String, String> OVERRIDE_MAP = new HashMap<String, String>();
		
		public EntityProperties() {		
		}
		
		public EntityProperty getProperty(String name) {
			if(REQUIRED_PROPERTY_MAP.containsKey(name)) {
				return REQUIRED_PROPERTY_MAP.get(name);
			} else if(OPTIONAL_PROPERTY_MAP.containsKey(name)) {
				return OPTIONAL_PROPERTY_MAP.get(name);
			} else {
				throw new NullPointerException("Entity property '" + name + "' is not valid!");
			}
		}
		
		public boolean isPropertyNameValid(String name) {
			// TODO: Create ignore_properties node in entity_properties.xml
			return REQUIRED_PROPERTY_MAP.containsKey(name) || OPTIONAL_PROPERTY_MAP.containsKey(name) ||
				   name.equals("body_skeleton_id") || name.equals("image_key");
		}
		
		public Array<String> getRequiredPropertyNames() {
			Array<String> names = new Array<String>();
			for(String name : REQUIRED_PROPERTY_MAP.keySet()) {
				names.add(name);
			}
			
			return names;
		}
		
		public Array<String> getOptionalPropertyNames() {
			Array<String> names = new Array<String>();
			for(String name : OPTIONAL_PROPERTY_MAP.keySet()) {
				names.add(name);
			}
			
			return names;
		}
		
		public Array<String> getOverridePropertyNames() {
			Array<String> names = new Array<String>();
			for(String name : OVERRIDE_MAP.keySet()) {
				names.add(name);
			}

			return names;
		}
		
		public void addOverride(String name, String value) {
			OVERRIDE_MAP.put(name, value);
		}
		
		public void addProperty(EntityProperty property) {
			if(property.required) {
				REQUIRED_PROPERTY_MAP.put(property.name, property);
			} else {
				OPTIONAL_PROPERTY_MAP.put(property.name, property);
			}
		}
	}
	
	public final class EntityProperty {
		
		public String name;
		public boolean required;
		public String defaultValue;
		public String note;
		
		public EntityProperty(String name, boolean required, String defaultValue, String note) {
			this.name = name;
			this.required = required;
			this.defaultValue = defaultValue;
			this.note = note;
		}
	}
}

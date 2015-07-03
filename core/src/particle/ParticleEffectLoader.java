package particle;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

import misc.Globals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public final class ParticleEffectLoader {
	
	private static final String CONFIG_FILENAME = "particle_effects.xml";	
	private static final HashMap<String, ParticleEffectProperty> PROPERTY_MAP = new HashMap<String, ParticleEffectProperty>();
	
	public ParticleEffectLoader() {
		
	}
	
	public void load() {
		try {
			XmlReader reader = new XmlReader();
			Element root = reader.parse(Gdx.files.internal(CONFIG_FILENAME));
			loadFromRoot(root);
		} catch(Exception e) {
			Gdx.app.error("collector", "ParticleEffectLoader", e);
		}
	}
	
	private void loadFromRoot(Element root) {
		Array<Element> propertyElems = root.getChildByName("properties").getChildrenByName("property");
		loadProperties(propertyElems);

		Array<Element> particleEffectElems = root.getChildByName("particle_effects").getChildrenByName("particle_effect");
		loadParticleEffects(particleEffectElems);
	}
	
	private void loadProperties(Array<Element> propertyElems) {
		for(Element propertyElem : propertyElems) {
			ParticleEffectProperty property = new ParticleEffectProperty(propertyElem);
			PROPERTY_MAP.put(property.name, property);
		}
	}
	
	private void loadParticleEffects(Array<Element> particleEffectElems) {
		for(Element particleEffectElem : particleEffectElems) {
			String particleEffectName = particleEffectElem.get("name");
			
			HashMap<String, String> propertyValueMap = new HashMap<String, String>();
			Array<Element> propertyElems = particleEffectElem.getChildByName("properties").getChildrenByName("property");
			for(Element propertyElem : propertyElems) {
				if(propertyElem.getBoolean("custom")) {
					continue;
				}
				
				String propertyName = propertyElem.get("name");
				String value = propertyElem.get("value");
				propertyValueMap.put(propertyName, value);
			}
			
			loadParticleEffect(particleEffectName, propertyValueMap);
		}
	}
	
	private void loadParticleEffect(String particleEffectName, HashMap<String, String> propertyValueMap) {
		ParticleEffect particleEffect = new ParticleEffect();
		for(Entry<String, String> entry : propertyValueMap.entrySet()) {
			String propertyName = entry.getKey();
			String value = entry.getValue();
			setProperty(particleEffect, propertyName, value);
		}
		
		Globals.getParticleEffectManager().addParticleEffect(particleEffectName, particleEffect);
	}
	
	private void setProperty(ParticleEffect particleEffect, String propertyName, String valueStr) {
		ParticleEffectProperty property = PROPERTY_MAP.get(propertyName);
		String type = property.type;
		String methodName = property.methodName;
		
		Method method;			
		try {
			if(type.equals("String")) {
				method = ParticleEffect.class.getMethod(methodName, String.class);  
    			method.invoke(particleEffect, valueStr);
			} else if(type.equals("Boolean")) {
    			method = ParticleEffect.class.getMethod(methodName, Boolean.class);  
    			method.invoke(particleEffect, toBoolean(valueStr));
    		} else if(type.equals("Integer")) {
    			method = ParticleEffect.class.getMethod(methodName, Integer.class); 
    			method.invoke(particleEffect, toInteger(valueStr));
    		} else if(type.equals("Float")) {
    			method = ParticleEffect.class.getMethod(methodName, Float.class);  
    			method.invoke(particleEffect, toFloat(valueStr));
    		} else if(type.equals("Color")) {
    			method = ParticleEffect.class.getMethod(methodName, Color.class);  
    			method.invoke(particleEffect, toColor(valueStr));
    		} else if(type.equals("Vector2")) {
    			method = ParticleEffect.class.getMethod(methodName, Float.class, Float.class);  
    			Vector2 vector2 = toVector2(valueStr);
    			method.invoke(particleEffect, vector2.x, vector2.y);
    		} else {
    			throw new NullPointerException("Particle effect property of type '" + type + "' is not valid");
    		}
		} catch(Exception e) {
			Gdx.app.error("collector", "ParticleEffectLoader", e);
		}
	}
	
	private Boolean toBoolean(String value) {
		return Boolean.valueOf(value);
	}
	
	private Integer toInteger(String value) {
		return Integer.valueOf(value);
	}
	
	private Float toFloat(String value) {
		return Float.valueOf(value);
	}
	
	private Color toColor(String value) {
		String[] components = value.split(", ");
		
		Color color = new Color();
		color.r = Float.valueOf(components[0]);
		color.g = Float.valueOf(components[1]);
		color.b = Float.valueOf(components[2]);
		color.a = 1;
		
		return color;
	}
	
	private Vector2 toVector2(String value) {
		String[] components = value.split(", ");
		
		Vector2 vector2 = new Vector2();
		return vector2.set(Float.valueOf(components[0]), Float.valueOf(components[1]));
	}
	
	private static class ParticleEffectProperty {
		
		public String name;
		public boolean required;
		public String type;
		public String methodName;

		public ParticleEffectProperty(Element propertyElem) {
			name = propertyElem.get("name");
			required = propertyElem.getBoolean("required");
			type = propertyElem.get("type");
			methodName = propertyElem.get("method", null);
		}
	}
}

package misc;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

import entity.Entity;

public final class Utils {
	
	public static boolean isPlayer(Entity entity) {
		return entity != null && entity.getType().equals("player");
	}
	
	public static boolean isPlayerShot(Entity entity) {
		return entity != null && entity.getType().equals("player_shot");
	}
	
	public static boolean isFromEntity(Fixture fixture) {
		BodyData bodyData = ((BodyData)fixture.getBody().getUserData());
		return bodyData.getEntity() != null;		
	}
	
	public static Entity getEntity(Fixture fixture) {
		BodyData bodyData = ((BodyData)fixture.getBody().getUserData());
		return bodyData.getEntity();
	}
	
	public static boolean usingAndroidContext() {
		return Gdx.app.getType() == ApplicationType.Android;
	}
	
	public static boolean usingDesktopContext() {
		return Gdx.app.getType() == ApplicationType.Desktop;
	}
	
	public static Object chooseRandom(Object[] objects) {
		return objects[MathUtils.random(0, objects.length - 1)];
	}
	
	public static boolean chooseRandom(boolean a, boolean b) {
		return MathUtils.random() < 0.5f ? a : b;
	}
	
	public static float chooseRandom(float a, float b) {
		return MathUtils.random() < 0.5f ? a : b;
	}
	
	public static int chooseRandom(int a, int b) {
		return MathUtils.random() < 0.5f ? a : b;
	}
	
	public static String chooseRandom(String a, String b) {
		return MathUtils.random() < 0.5f ? a : b;
	}
	
	public static float getRandomFromRange(Vector2 range) {
		return MathUtils.random(range.x, range.y);
	}

	public static float getRandomFromRange(float a, float b, float split) {
		if(split == 0) {
			return MathUtils.random(a, b);
		}
		
		return Utils.chooseRandom(MathUtils.random(a, -split), MathUtils.random(split, b));
	}
	
	public static void shuffleArray(Object[] array) {
	    int index;
	    Object temp;
	    for (int i = array.length - 1; i > 0; i--) {
	        index = MathUtils.random(0,  i);
	        temp = array[index];
	        array[index] = array[i];
	        array[i] = temp;
	    }
	}
	
	public static boolean propertyExists(MapObject mapObject, String key) {
		MapProperties properties = mapObject.getProperties();
		return properties.containsKey(key);
	}
	
	public static boolean getPropertyBoolean(MapObject mapObject, String key) {
		MapProperties properties = mapObject.getProperties();
		return Boolean.parseBoolean(properties.get(key).toString());
	}
	
	public static int getPropertyInt(MapObject mapObject, String key) {
		MapProperties properties = mapObject.getProperties();
		return Integer.parseInt(properties.get(key).toString());
	}
	
	public static float getPropertyFloat(MapObject mapObject, String key) {
		MapProperties properties = mapObject.getProperties();
		return Float.parseFloat(properties.get(key).toString());
	}
	
	public static String getPropertyString(MapObject mapObject, String key) {
		MapProperties properties = mapObject.getProperties();
		return properties.get(key).toString();
	}
	
	public static Vector2 getPropertyVector2(MapObject mapObject, String key) {
		if(getPropertyString(mapObject, key).isEmpty()) {
			return new Vector2();
		}
		
		float[] vals = getPropertyFloatArray(mapObject, key, ",");
		return new Vector2(vals[0], vals[1]);
		
	}
	
	public static boolean[] getPropertyBooleanArray(MapObject mapObject, String key, String delim) {
		String full = getPropertyString(mapObject, key);
		
		if(full.isEmpty()) {
			return new boolean[0];
		}
		
		String[] strArr = full.split(delim);
		boolean[] booleanArr = new boolean[strArr.length];
		
		int i = 0;
		for(String elem : strArr) {
			booleanArr[i++] = Boolean.parseBoolean(elem);
		}
		
		return booleanArr;
	}
	
	public static int[] getPropertyIntArray(MapObject mapObject, String key, String delim) {
		String full = getPropertyString(mapObject, key);
		
		if(full.isEmpty()) {
			return new int[0];
		}
		
		String[] strArr = full.split(delim);
		int[] intArr = new int[strArr.length];
		
		int i = 0;
		for(String elem : strArr) {
			intArr[i++] = Integer.parseInt(elem);
		}
		
		return intArr;
	}
	
	public static float[] getPropertyFloatArray(MapObject mapObject, String key, String delim) {
		String full = getPropertyString(mapObject, key);
		
		if(full.isEmpty()) {
			return new float[0];
		}
		
		String[] strArr = full.split(delim);
		float[] floatArr = new float[strArr.length];
		
		int i = 0;
		for(String elem : strArr) {
			floatArr[i++] = Float.parseFloat(elem);
		}
		
		return floatArr;
	}
	
	public static String[] getPropertyStringArray(MapObject mapObject, String key, String delim) {
		String full = getPropertyString(mapObject, key);
		
		if(full.isEmpty()) {
			return new String[0];
		}
		
		return full.split(delim);
	}

	public static FixtureDef getFixtureDefFromBodySkeleton(MapObject object) {
		return getFixtureDefFromBodySkeleton(object, 1);
	}
	
	public static FixtureDef getScaledFixtureDefFromBodySkeleton(MapObject object, float scale) {
		return getFixtureDefFromBodySkeleton(object, scale);
	}
	
	private static FixtureDef getFixtureDefFromBodySkeleton(MapObject object, float scale) {
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = 1;
		fixtureDef.friction = 0;
		fixtureDef.restitution = 0;
		
		Shape shape = null;
		float unitScale = Globals.getCamera().getTileMapScale();
		if(object instanceof TextureMapObject) {
			shape = getTextureMapShape(object, unitScale, scale);
		} else if(object instanceof RectangleMapObject) {
			shape = getRectangleShape(object, unitScale, scale);
		} else if(object instanceof PolylineMapObject) {
			shape = getPolylineShape(object, unitScale, scale);
		} else if(object instanceof CircleMapObject) {
			shape = getCircleShape(object, unitScale, scale);
		} else if(object instanceof EllipseMapObject) {
			shape = getEllipseShape(object, unitScale, scale);
		} else if(object instanceof PolygonMapObject) {
			shape = getPolygonShape(object, unitScale, scale);
		} 
		
		fixtureDef.shape = shape;
		
		return fixtureDef;
	}
	
	private static Shape getTextureMapShape(MapObject object, float unitScale, float scale) {
		TextureMapObject textureMapObject = (TextureMapObject)object;
		float width = (Float)textureMapObject.getProperties().get("width");
		float height = (Float)textureMapObject.getProperties().get("height");
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width / 2 * unitScale * scale, height / 2 * unitScale * scale);
		return shape;
	}
	
	private static Shape getRectangleShape(MapObject object, float unitScale, float scale) {
		Rectangle rectangle = ((RectangleMapObject)object).getRectangle();
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(rectangle.width / 2 * unitScale * scale, rectangle.height / 2 * unitScale * scale);
		return shape;
	}
	
	private static Shape getPolylineShape(MapObject object, float unitScale, float scale) {
		Polyline polyline = ((PolylineMapObject)object).getPolyline();
		float[] vertices = polyline.getTransformedVertices();
		for(int i = 0; i < vertices.length; i++) {
			vertices[i] *= Globals.getCamera().getTileMapScale() * scale;
		}
		
		ChainShape shape = new ChainShape();
		shape.createChain(vertices);
		
		return shape;
	}
	
	private static Shape getCircleShape(MapObject object, float unitScale, float scale) {
		Circle circle = ((CircleMapObject)object).getCircle();
    	CircleShape shape = new CircleShape();
    	shape.setRadius(circle.radius * unitScale * scale);
    	
    	return shape;
	}
	
	// Just assume the ellipse is a circle.
	private static Shape getEllipseShape(MapObject object, float unitScale, float scale) {
		Ellipse circle = ((EllipseMapObject)object).getEllipse();
		CircleShape shape = new CircleShape();
    	shape.setRadius(circle.width / 2 * unitScale * scale);
    	
    	return shape;
	}
	
	private static Shape getPolygonShape(MapObject object, float unitScale, float scale) {
		Polygon polygon = ((PolygonMapObject)object).getPolygon();
		float[] vertices = polygon.getTransformedVertices();
		for(int i = 0; i < vertices.length; i++) {
			vertices[i] *= Globals.getCamera().getTileMapScale() * scale;
		}
		
		PolygonShape shape = new PolygonShape();
		shape.set(vertices);
		
		return shape;
	}
}

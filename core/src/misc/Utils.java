package misc;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

import entity.Entity;

public class Utils {
	
	public static boolean isPlayer(Entity entity) {
		return entity.getType().equals("player");
	}
	
	public static boolean isShot(Entity entity) {
		return entity.getType().equals("player_shot");
	}
	
	public static FixtureDef getFixtureDefFromBodySkeleton(MapObject object) {
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = 1;
		fixtureDef.friction = 0;
		fixtureDef.restitution = 0;
		
		Shape shape = null;
		float unitScale = Globals.getCamera().getTileMapScale();
		if(object instanceof TextureMapObject) {
			shape = getTextureMapShape(object, unitScale);
		} else if(object instanceof RectangleMapObject) {
			shape = getRectangleShape(object, unitScale);
		} else if(object instanceof PolylineMapObject) {
			shape = getPolylineShape(object, unitScale);
		} else if(object instanceof CircleMapObject) {
			shape = getCircleShape(object, unitScale);
		} else if(object instanceof EllipseMapObject) {
			shape = getEllipseShape(object, unitScale);
		} else if(object instanceof PolygonMapObject) {
			shape = getPolygonShape(object, unitScale);
		} 
		
		fixtureDef.shape = shape;
		
		return fixtureDef;
	}
	
	private static Shape getTextureMapShape(MapObject object, float unitScale) {
		TextureMapObject textureMapObject = (TextureMapObject)object;
		float width = (Float)textureMapObject.getProperties().get("width");
		float height = (Float)textureMapObject.getProperties().get("height");
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width / 2 * unitScale, height / 2 * unitScale);
		return shape;
	}
	
	private static Shape getRectangleShape(MapObject object, float unitScale) {
		Rectangle rectangle = ((RectangleMapObject)object).getRectangle();
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(rectangle.width / 2 * unitScale, rectangle.height / 2 * unitScale);
		return shape;
	}
	
	private static Shape getPolylineShape(MapObject object, float unitScale) {
		Polyline polyline = ((PolylineMapObject)object).getPolyline();
		float[] vertices = polyline.getTransformedVertices();
		for(int i = 0; i < vertices.length; i++) {
			vertices[i] *= Globals.getCamera().getTileMapScale();
		}
		
		ChainShape shape = new ChainShape();
		shape.createChain(vertices);
		
		return shape;
	}
	
	private static Shape getCircleShape(MapObject object, float unitScale) {
		Circle circle = ((CircleMapObject)object).getCircle();
    	CircleShape shape = new CircleShape();
    	shape.setRadius(circle.radius * unitScale);
    	
    	return shape;
	}
	
	// Just assume the ellipse is a circle.
	private static Shape getEllipseShape(MapObject object, float unitScale) {
		Ellipse circle = ((EllipseMapObject)object).getEllipse();
		CircleShape shape = new CircleShape();
    	shape.setRadius(circle.width / 2 * unitScale);
    	
    	return shape;
	}
	
	private static Shape getPolygonShape(MapObject object, float unitScale) {
		Polygon polygon = ((PolygonMapObject)object).getPolygon();
		return null;
	}
}

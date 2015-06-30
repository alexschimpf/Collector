package core;

import java.util.HashMap;
import java.util.Iterator;

import misc.BodyData;
import misc.Globals;
import misc.Utils;
import animation.Animation;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;

import entity.Entity;
import entity.EntityBodyDef;
import entity.special.Player;

public final class GameWorldLoader {
	
	private final HashMap<String, MapObject> BODY_SKELETON_MAP = new HashMap<String, MapObject>();
	
	private TiledMap tileMap;
	
	public GameWorldLoader(TiledMap tileMap) {
		this.tileMap = tileMap;
		Globals.getPhysicsWorld();
	}
	
	public void load() {
		MapLayers layers = tileMap.getLayers();
		Iterator<MapLayer> layerIter = layers.iterator();
		while(layerIter.hasNext()) {
			loadLayer(layerIter.next());
		}
	}
	
	private void loadLayer(MapLayer layer) {
		Array<MapObject> bodyObjects = new Array<MapObject>();
		Array<TextureMapObject> entityObjects = new Array<TextureMapObject>();
		Array<MapObject> animationObjects = new Array<MapObject>();
		
		if(layer instanceof TiledMapTileLayer) {
			TiledMapTileLayer tileLayer = (TiledMapTileLayer)layer;
	        for(int col = 0; col < tileLayer.getWidth(); col++){
	            for(int row = 0; row < tileLayer.getHeight(); row++){
	                TiledMapTileLayer.Cell cell = tileLayer.getCell(col, row);
	                if(cell == null) {
	                	continue;
	                }
	                
	                cell.setFlipVertically(true);
	            }
	        }
		}
			
		for(MapObject object : layer.getObjects()) {
			String type = (String)object.getProperties().get("type");
			
			if(object instanceof TextureMapObject) {
				TextureMapObject textureObject = (TextureMapObject)object;
				entityObjects.add(textureObject);
			} else {				
				if(type == null) {
					bodyObjects.add(object);
					continue;
				}
				
				if(type.equals("body_skeleton")) {
					BODY_SKELETON_MAP.put(object.getName(), object);
				} else if(type.equals("animation")) {
					animationObjects.add(object);
				}
			}
		}
		
		loadBodies(bodyObjects);
		loadEntities(entityObjects, BODY_SKELETON_MAP);
		loadAnimations(animationObjects);
	}
	
	private void loadBodies(Array<MapObject> objects) {
		for(MapObject object : objects) {
			if(object instanceof RectangleMapObject) {
				loadBodyFromRectangle(object);
			} else if(object instanceof PolylineMapObject) {
				loadBodyFromPolyline(object);
			} else if(object instanceof CircleMapObject) {
				loadBodyFromCircle(object);
			} else if(object instanceof EllipseMapObject) {
				loadBodyFromEllipse(object);
			} else if(object instanceof PolygonMapObject) {
				loadBodyFromPolygon(object);
			}
		}
	}
	
	private void loadEntities(Array<TextureMapObject> objects, HashMap<String, MapObject> bodySkeletonMap) {
		for(TextureMapObject object : objects) {
			MapProperties properties = object.getProperties();
			String type = (String)properties.get("type");
			if(type == null) {
				throw new NullPointerException("TextureMapObject has no type");
			}
			
			MapObject bodySkeleton = null;
			if(properties.containsKey("body_skeleton_id")) {
				String bodySkeletonId = (String)properties.get("body_skeleton_id");			
				if(!bodySkeletonMap.containsKey(bodySkeletonId)) {
					throw new NullPointerException("Body skeleton id '" + bodySkeletonId + "' is not valid");
				}
				
				bodySkeleton = bodySkeletonMap.get(bodySkeletonId);
			}
			
			if(bodySkeleton == null) {
				bodySkeleton = object;
			}
			
			EntityPropertyValidator validator = Globals.getEntityPropertyValidator();
			validator.validateAndProcess(type, object.getProperties());
			
			EntityBodyDef bodyDef = getBodyDef(object.getProperties());
			Entity entity = validator.getEntity(bodyDef, object, bodySkeleton);
			entity.setBodyData();
			
			if(type.equals("player")) {
				Globals.getGameWorld().setPlayer((Player)entity);
			}

			Globals.getGameWorld().addEntity(entity);
		}
	}
	
	private void loadAnimations(Array<MapObject> animationObjects) {
		for(MapObject object : animationObjects) {
			MapProperties properties = object.getProperties();
			if(!properties.containsKey("animation_key")) {
				throw new NullPointerException("Animation object does not contain an 'animation_key' property");
			} else if(!properties.containsKey("total_duration")) {
				throw new NullPointerException("Animation object does not contain an 'total_duration' property");
			}
			
			String animationKey = (String)properties.get("animation_key");
			if(Globals.getTextureManager().getAnimationTextures(animationKey) == null) {
				throw new NullPointerException("Animation with key '" + animationKey + "' does not exist");
			}
			
			Float totalDuration = (Float)properties.get("total_duration");	
			Boolean loop = (Boolean)properties.get("loop");
			Globals.getGameScreen().addAnimation(
        		new Animation.Builder(animationKey, getObjectPosition(properties), getObjectSize(properties), totalDuration)
        		.loop(loop != null ? loop : true)
        		.playOnCreate(true)
        		.build()
			);
		}
	}
	
	private void loadBodyFromRectangle(MapObject object) {
		Rectangle rectangle = ((RectangleMapObject)object).getRectangle();
		
		float unitScale = Globals.getCamera().getTileMapScale();	
		float left = rectangle.x * unitScale;
		float top = rectangle.y * unitScale;
		float width = rectangle.width * unitScale;
		float height = rectangle.height * unitScale;
		
		BodyDef bodyDef = new BodyDef();
	    bodyDef.position.x = left + (width / 2);
	    bodyDef.position.y = top + (height / 2);
	    bodyDef.type = BodyType.StaticBody;
	    
	    FixtureDef fixtureDef = Utils.getFixtureDefFromBodySkeleton(object);
		
		Body body = Globals.getPhysicsWorld().createBody(bodyDef);
		body.createFixture(fixtureDef);
		
		body.setUserData(new BodyData(null));
		
		fixtureDef.shape.dispose();
	}
	
	private void loadBodyFromPolyline(MapObject object) {
		BodyDef bodyDef = new BodyDef();
	    bodyDef.position.set(0, 0);
	    bodyDef.type = BodyType.StaticBody;
	    
	    FixtureDef fixtureDef = Utils.getFixtureDefFromBodySkeleton(object);
		
		Body body = Globals.getPhysicsWorld().createBody(bodyDef);
		body.createFixture(fixtureDef);
		
		body.setUserData(new BodyData(null));
		
		fixtureDef.shape.dispose();
	}

    private void loadBodyFromCircle(MapObject object) {
    	Circle circle = ((CircleMapObject)object).getCircle();
    	
    	float unitScale = Globals.getCamera().getTileMapScale();
    	float x = circle.x * unitScale;
    	float y = circle.y * unitScale;

    	BodyDef bodyDef = new BodyDef();
	    bodyDef.position.x = x;
	    bodyDef.position.y = y;
	    bodyDef.type = BodyType.StaticBody;
    	
    	FixtureDef fixtureDef = Utils.getFixtureDefFromBodySkeleton(object);
    			
		Body body = Globals.getPhysicsWorld().createBody(bodyDef);
		body.createFixture(fixtureDef);
		
		body.setUserData(new BodyData(null));
		
		fixtureDef.shape.dispose();
    }
    
    // Just assume the ellipse is a circle.
    private void loadBodyFromEllipse(MapObject object) {
    	Ellipse circle = ((EllipseMapObject)object).getEllipse();
    	
    	float unitScale = Globals.getCamera().getTileMapScale();
    	float x = (circle.x + circle.width / 2) * unitScale;
    	float y = (circle.y + circle.height / 2) * unitScale;

    	BodyDef bodyDef = new BodyDef();
	    bodyDef.position.x = x;
	    bodyDef.position.y = y;
	    bodyDef.type = BodyType.StaticBody;
    	
    	FixtureDef fixtureDef = Utils.getFixtureDefFromBodySkeleton(object);
    			
		Body body = Globals.getPhysicsWorld().createBody(bodyDef);
		body.createFixture(fixtureDef);
		
		body.setUserData(new BodyData(null));
		
		fixtureDef.shape.dispose();
    }
    
    private void loadBodyFromPolygon(MapObject object) {
    	BodyDef bodyDef = new BodyDef();
	    bodyDef.position.set(0, 0);
	    bodyDef.type = BodyType.StaticBody;
	    
	    FixtureDef fixtureDef = Utils.getFixtureDefFromBodySkeleton(object);
		
		Body body = Globals.getPhysicsWorld().createBody(bodyDef);
		body.createFixture(fixtureDef);
		
		body.setUserData(new BodyData(null));
		
		fixtureDef.shape.dispose();
    }
    
    private EntityBodyDef getBodyDef(MapProperties properties) {
    	if(!properties.containsKey("body_type")) {
    		throw new NullPointerException("TextureMapObject does not contain property 'body_type'");
    	}
    		
    	String bodyTypeStr = (String)properties.get("body_type");
    	BodyType bodyType;
    	if(bodyTypeStr.equals("static")) {
    		bodyType = BodyType.StaticBody;
    	} else if(bodyTypeStr.equals("kinematic")) {
    		bodyType = BodyType.KinematicBody;
    	} else if(bodyTypeStr.equals("dynamic")) {
    		bodyType = BodyType.DynamicBody;
    	} else {
    		throw new NullPointerException("TextureMapObject does not contain valid 'body_type' property");
    	}
 
    	return new EntityBodyDef(getObjectPosition(properties), getObjectSize(properties), bodyType);
    }
    
    private Vector2 getObjectSize(MapProperties properties) {
    	float unitScale = Globals.getCamera().getTileMapScale();
    	float width = (Float)properties.get("width") * unitScale;
    	float height = (Float)properties.get("height") * unitScale;
    	
    	return new Vector2(width, height);
    }
    
    private Vector2 getObjectPosition(MapProperties properties) {
    	float unitScale = Globals.getCamera().getTileMapScale();
    	float width = (Float)properties.get("width") * unitScale;
    	float height = (Float)properties.get("height") * unitScale;
    	float x = (Float)properties.get("x") * unitScale;
    	float y = (Float)properties.get("y") * unitScale;
    	
    	x += width / 2;
    	y -= height / 2;
    	
    	return new Vector2(x, y);
    }
}

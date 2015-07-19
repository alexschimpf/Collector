package core;

import java.util.HashMap;
import java.util.LinkedList;

import misc.BodyData;
import misc.Globals;
import misc.Utils;
import animation.Animation;

import com.badlogic.gdx.maps.MapLayer;
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
	
	private final TiledMap TILE_MAP;
	private final GameRoom ROOM;
	
	public GameWorldLoader(TiledMap tileMap, boolean isLobby) {
		TILE_MAP = tileMap;
		ROOM = new GameRoom(isLobby);
		
		Globals.getPhysicsWorld();
	}
	
	public void load() {
		Globals.getGameWorld().clearPhysicsWorld();
		
		Globals.getGameWorld().setCurrentRoom(ROOM);		
		
		LinkedList<MapLayer> orderedLayers = new LinkedList<MapLayer>();
		for(MapLayer layer : TILE_MAP.getLayers()) {
			if(layer.getName().equals("Bodies")) {
				orderedLayers.addFirst(layer);
			} else {
				orderedLayers.addLast(layer);
			}
		}
		
		for(MapLayer layer : orderedLayers) {
			loadLayer(layer);
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
				} else if(type.equals("body_skeleton")) {
					BODY_SKELETON_MAP.put(object.getName(), object);
				} else if(type.equals("animation")) {
					animationObjects.add(object);
				} else {
					bodyObjects.add(object);
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
			String type = Utils.getPropertyString(object, "type");
			if(type == null) {
				throw new NullPointerException("TextureMapObject has no type");
			}
			
			if(properties.containsKey("is_script") && Utils.getPropertyBoolean(object, "is_script")) {
				ROOM.addScriptTemplate(object);
				continue;
			}
			
			EntityPropertyValidator validator = Globals.getEntityPropertyValidator();
			validator.validateAndProcess(type, object.getProperties());
			
			MapObject bodySkeleton = null;
			if(properties.containsKey("body_skeleton_id")) {
				String bodySkeletonId = Utils.getPropertyString(object, "body_skeleton_id");			
				if(!bodySkeletonMap.containsKey(bodySkeletonId)) {
					throw new NullPointerException("Body skeleton id '" + bodySkeletonId + "' is not valid");
				}
				
				bodySkeleton = bodySkeletonMap.get(bodySkeletonId);
			}
			
			if(!properties.get("body_width").toString().isEmpty() && 
			   !properties.get("body_height").toString().isEmpty()) {
				float bodyWidth = Utils.getPropertyFloat(object, "body_width");
				float bodyHeight = Utils.getPropertyFloat(object, "body_height");
				RectangleMapObject rectMapObj = new RectangleMapObject();
				rectMapObj.getRectangle().width = bodyWidth;
				rectMapObj.getRectangle().height = bodyHeight;
				
				bodySkeleton = rectMapObj;
			}
			
			if(bodySkeleton == null) {
				bodySkeleton = object;
			}
			
			EntityBodyDef bodyDef = getBodyDef(object);
			Entity entity = validator.getEntity(bodyDef, object, bodySkeleton);
			entity.setBodyData();
			
			if(type.equals("player")) {
				Globals.getGameWorld().setPlayer((Player)entity);
			}

			ROOM.addEntity(entity);
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
			
			String animationKey = Utils.getPropertyString(object, "animation_key");
			if(Globals.getTextureManager().getAnimationTextures(animationKey) == null) {
				throw new NullPointerException("Animation with key '" + animationKey + "' does not exist");
			}
			
			Float totalDuration = Utils.getPropertyFloat(object, "total_duration");	
			Boolean loop = Utils.getPropertyBoolean(object, "loop");
			Globals.getGameScreen().addAnimation(
        		new Animation.Builder(animationKey, getObjectPosition(object), getObjectSize(object), totalDuration)
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
		
		if(Utils.propertyExists(object, "type") && Utils.getPropertyString(object, "type").equals("room_entrance")) {
			String tileMapName = Utils.getPropertyString(object, "room_tile_map");
			Rectangle scaledRect = new Rectangle(left, top, width, height);
			Globals.getCurrentRoom().addRoomEntranceLocation(tileMapName, scaledRect);
			return;
		}
		
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
    
    private EntityBodyDef getBodyDef(MapObject object) {
    	MapProperties properties = object.getProperties();
    	if(!properties.containsKey("body_type")) {
    		throw new NullPointerException("TextureMapObject does not contain property 'body_type'");
    	}
    		
    	String bodyTypeStr = Utils.getPropertyString(object, "body_type");
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
 
    	return new EntityBodyDef(getObjectPosition(object), getObjectSize(object), bodyType);
    }
    
    private Vector2 getObjectSize(MapObject object) {
    	float unitScale = Globals.getCamera().getTileMapScale();
    	float width = Utils.getPropertyFloat(object, "width") * unitScale;
    	float height = Utils.getPropertyFloat(object, "height") * unitScale;
    	
    	return new Vector2(width, height);
    }
    
    private Vector2 getObjectPosition(MapObject object) {
    	float unitScale = Globals.getCamera().getTileMapScale();
    	float width = Utils.getPropertyFloat(object, "width") * unitScale;
    	float height = Utils.getPropertyFloat(object, "height") * unitScale;
    	float x = Utils.getPropertyFloat(object, "x") * unitScale;
    	float y = Utils.getPropertyFloat(object, "y") * unitScale;
    	
    	x += width / 2;
    	y -= height / 2;
    	
    	return new Vector2(x, y);
    }
}

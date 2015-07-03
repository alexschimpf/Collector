package core;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import misc.CollisionListener;
import misc.Globals;
import misc.Globals.State;
import misc.IRender;
import misc.IUpdate;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import entity.Entity;
import entity.special.Player;

public final class GameWorld implements IRender, IUpdate {
	
	private final static float DEFAULT_GRAVITY = 20;
	
	public static GameWorld instance;
	
	private final World PHYSICS_WORLD = new World(new Vector2(0, DEFAULT_GRAVITY), true);
	private final ConcurrentHashMap<String, Entity> ENTITY_MAP = new ConcurrentHashMap<String, Entity>();
	
	private Player player;
	
	public static GameWorld getInstance() {
		if(instance == null) {
			instance = new GameWorld();
		}
		
		return instance;
	}
	
	private GameWorld() {		
		World.setVelocityThreshold(0.5f);
		
		PHYSICS_WORLD.setContactListener(new CollisionListener());
	}
	
	@Override
	public void render(SpriteBatch spriteBatch) {
		for(Entity entity : getEntities()) {
			entity.render(spriteBatch);
		}
	}
	
	@Override
	public boolean update() {
		PHYSICS_WORLD.step(1 / 45.0f, 5, 5);

		updateEntities();
		
		return false;
	}

	@Override
	public void done() {
		PHYSICS_WORLD.dispose();
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Collection<Entity> getEntities() {
		return ENTITY_MAP.values();
	}
	
	public Entity getEntityById(String id) {
		return ENTITY_MAP.get(id);
	}
	
	public void removeEntityById(String id) {
		ENTITY_MAP.remove(id);
	}
	
	public void removeEntity(Entity entity) {
		ENTITY_MAP.remove(entity.getId());
	}
	
	public void addEntity(Entity entity) {
		ENTITY_MAP.put(entity.getId(), entity);
	}
	
	private void updateEntities() {
		Iterator<Entry<String, Entity>> entitiesIter = ENTITY_MAP.entrySet().iterator();
		while(entitiesIter.hasNext()) {
			Entity entity = entitiesIter.next().getValue();
			if(entity.update()) {
				entity.done();			
				entitiesIter.remove();
			}
 		}
	}
	
	// TODO: Optimize this.
	private final Rectangle r1 = new Rectangle();
	private final Rectangle r2 = new Rectangle();
	public boolean isEntityAt(float left, float top, float width, float height, Entity ignore) {
		r1.set(left, top, width, height);
		
		for(Entity entity : getEntities()) {
			if(ignore != null && entity.equals(ignore)) {
				continue;
			}
			
			r2.set(entity.getLeft(), entity.getTop(), entity.getWidth(), entity.getHeight());
			if(r1.overlaps(r2)) {
				return true;
			}
		}
			
		return false;
	}
	
	public World getWorld() {
		return PHYSICS_WORLD;
	}
	
	public Array<Body> getBodies() {
		Array<Body> bodies = new Array<Body>();
		PHYSICS_WORLD.getBodies(bodies);
		return bodies;
	}

	public float getLeft() {
		return 0;
	}
	
	public float getRight() {
		return Globals.getTileSize() * Globals.NUM_TILE_MAP_COLS;
	}
	
	public float getTop() {
		return 0;
	}
	
	public float getBottom() {
		return Globals.getTileSize() * Globals.NUM_TILE_MAP_ROWS;
	}
	
	public float getWidth() {
		return getRight();
	}
	
	public float getHeight() {
		return getBottom();
	}
}
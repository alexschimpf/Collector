package core;

import java.util.Iterator;

import misc.CollisionListener;
import misc.Globals;
import misc.IRender;
import misc.IUpdate;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import entity.special.Player;

public final class GameWorld implements IRender, IUpdate {
	
	private final static float DEFAULT_GRAVITY = 20;
	
	public static GameWorld instance;
	
	private final World PHYSICS_WORLD = new World(new Vector2(0, DEFAULT_GRAVITY), true);
	
	private Player player;
	private GameRoom currRoom;
	private String lobbyTileMapName;
	
	private GameWorld() {		
		World.setVelocityThreshold(0.5f);
		
		PHYSICS_WORLD.setContactListener(new CollisionListener());
	}
	
	public static GameWorld getInstance() {
		if(instance == null) {
			instance = new GameWorld();
		}
		
		return instance;
	}
	
	@Override
	public void render(SpriteBatch spriteBatch) {
		currRoom.render(spriteBatch);
	}
	
	@Override
	public boolean update() {
		PHYSICS_WORLD.step(1 / 45.0f, 5, 5);

		currRoom.update();
		
		return false;
	}

	@Override
	public void done() {
		PHYSICS_WORLD.dispose();
	}
	
	public void clearPhysicsWorld() {
		Iterator<Body> bodiesIter = getBodies().iterator();
		while(bodiesIter.hasNext()) {
			Body body = bodiesIter.next();
			PHYSICS_WORLD.destroyBody(body);
		}
	}
	
	public void loadRoom(String tileMapName, boolean isLobby) {
		TileMap tileMap = new TileMap(tileMapName);
		Globals.getGameScreen().setTileMap(tileMap);
		GameWorldLoader gameWorldLoader = new GameWorldLoader(tileMap.getRawTileMap(), isLobby);
		gameWorldLoader.load();
		
		if(isLobby) {
			lobbyTileMapName = tileMapName;
		}
	}
	
	public void loadLobbyRoom() {
		loadRoom(lobbyTileMapName, true);
	}

	public void setCurrentRoom(GameRoom room) {
		currRoom = room;
	}

	public GameRoom getCurrentRoom() {
		return currRoom;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
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
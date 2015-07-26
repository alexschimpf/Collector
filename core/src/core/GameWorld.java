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
	
	private final World _physicsWorld = new World(new Vector2(0, DEFAULT_GRAVITY), true);
	
	private Player _player;
	private GameRoom _currRoom;
	private String _lobbyTileMapName;
	
	private GameWorld() {		
		World.setVelocityThreshold(0.5f);
		
		_physicsWorld.setContactListener(new CollisionListener());
	}
	
	public static GameWorld getInstance() {
		if(instance == null) {
			instance = new GameWorld();
		}
		
		return instance;
	}
	
	@Override
	public void render(SpriteBatch spriteBatch) {
		_currRoom.render(spriteBatch);
		
		_player.render(spriteBatch);
	}
	
	@Override
	public boolean update() {
		_physicsWorld.step(1 / 45.0f, 5, 5);

		_currRoom.update();
		
		return false;
	}

	@Override
	public void done() {
		_physicsWorld.dispose();
	}
	
	public void clearPhysicsWorld() {
		Iterator<Body> bodiesIter = getBodies().iterator();
		while(bodiesIter.hasNext()) {
			Body body = bodiesIter.next();
			_physicsWorld.destroyBody(body);
		}
	}
	
	public void loadRoom(String tileMapName, boolean isLobby) {
		WeatherSystem weatherSystem = Globals.getWeatherSystem();
		if(isLobby) {
			weatherSystem.setEnabled(false);
			weatherSystem.clearClouds();
		} else {
			weatherSystem.setEnabled(true);
			weatherSystem.resetClouds(true);
		}
		
		TileMap tileMap = new TileMap(tileMapName);
		Globals.getGameScreen().setTileMap(tileMap);
		GameWorldLoader gameWorldLoader = new GameWorldLoader(tileMap.getRawTileMap(), isLobby);
		gameWorldLoader.load();
		
		if(isLobby) {
			_lobbyTileMapName = tileMapName;
		}
	}
	
	public void loadLobbyRoom() {
		loadRoom(_lobbyTileMapName, true);
	}

	public void setCurrentRoom(GameRoom room) {
		_currRoom = room;
	}

	public GameRoom getCurrentRoom() {
		return _currRoom;
	}

	public void setPlayer(Player player) {
		this._player = player;
	}
	
	public Player getPlayer() {
		return _player;
	}
	
	public World getWorld() {
		return _physicsWorld;
	}
	
	public Array<Body> getBodies() {
		Array<Body> bodies = new Array<Body>();
		_physicsWorld.getBodies(bodies);
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
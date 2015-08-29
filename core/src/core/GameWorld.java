package core;

import java.util.HashMap;
import java.util.Iterator;

import misc.CollisionListener;
import misc.Globals;
import misc.Globals.State;
import misc.IRender;
import misc.IUpdate;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import entity.Player;

public final class GameWorld implements IRender, IUpdate {
	
	public final static float DEFAULT_GRAVITY = 20;
	
	public static GameWorld instance;
	
	private final World _physicsWorld = new World(new Vector2(0, DEFAULT_GRAVITY), true);
	private final HashMap<String, WeatherSystem> roomWeatherSystemMap = new HashMap<String, WeatherSystem>();
	
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
	
	public void renderEnclosing(SpriteBatch spriteBatch) {
		_currRoom.renderEnclosing(spriteBatch);
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
		String prevTileMapName = null;
		if(isLobby) {
			_lobbyTileMapName = tileMapName;
			
			if(_currRoom != null) {
				prevTileMapName = _currRoom.getTileMapName();
			}
			
			if(prevTileMapName == tileMapName) {
				return;
			}
		}
		
		boolean isPlayerFacingRight = true;
		if(Globals.getPlayer() != null) {
			isPlayerFacingRight = Globals.getPlayer().isFacingRight();
		}

		Globals.state = Globals.State.LOADING;

		TileMap tileMap = new TileMap(tileMapName);
		Globals.getGameScreen().setTileMap(tileMap);
		GameWorldLoader gameWorldLoader = new GameWorldLoader(tileMap.getRawTileMap(), tileMapName, isLobby);
		gameWorldLoader.load();
		
		if(prevTileMapName != null) {
			Rectangle entrance = _currRoom.getRoomEntranceLocation(prevTileMapName); 
			Globals.getPlayer().setPosition(entrance.getX() + (entrance.getWidth() / 2), entrance.getY() + entrance.getHeight());
		}
		
		WeatherSystem weatherSystem = Globals.getWeatherSystem();
		if(isLobby) {
			weatherSystem.clearClouds();
			weatherSystem.setEnabled(false);
		} else {
			if(roomWeatherSystemMap.containsKey(tileMapName)) {
				WeatherSystem roomWeatherSystem = roomWeatherSystemMap.get(tileMapName);
				Globals.getWeatherSystem().set(roomWeatherSystem);
				weatherSystem.setEnabled(true);
			} else {				
				weatherSystem.resetClouds(true);
				weatherSystem.setEnabled(true);
				roomWeatherSystemMap.put(tileMapName, weatherSystem.clone());
			}
		}
		
		Globals.getPhysicsWorld().setGravity(new Vector2(0, GameWorld.DEFAULT_GRAVITY));
		
		Player player = Globals.getPlayer();
		player.setFacingRight(isPlayerFacingRight);
		player.setGravityPipe(null);
		
		// HACK: Avoids a fade transition when first loading up.
		if(prevTileMapName == null && _currRoom.isLobby()) {
			Globals.state = State.RUNNING;
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
		return Globals.getTileSize() * _currRoom.getNumCols();
	}
	
	public float getTop() {
		return 0;
	}
	
	public float getBottom() {
		return Globals.getTileSize() * _currRoom.getNumRows();
	}
	
	public float getWidth() {
		return getRight();
	}
	
	public float getHeight() {
		return getBottom();
	}
}
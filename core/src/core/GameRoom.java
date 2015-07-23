package core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import misc.Globals;
import misc.IRender;
import misc.IUpdate;
import script.Script;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import entity.Entity;
import entity.special.Player;

public class GameRoom implements IRender, IUpdate {

	private final ConcurrentHashMap<String, Entity> _entityMap = new ConcurrentHashMap<String, Entity>();
	private final ConcurrentHashMap<String, MapObject> _scriptTemplateMap = new ConcurrentHashMap<String, MapObject>();
	private final Array<Script> _activeScripts = new Array<Script>();
	private final HashMap<String, Rectangle> _roomEntranceLocationMap = new HashMap<String, Rectangle>();
	
	private boolean _isLobby;
	
	public GameRoom(boolean isLobby) {	
		this._isLobby = isLobby;
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		for(Entity entity : getEntities()) {
			if(!entity.getType().equals("player")) {
				entity.render(spriteBatch);
			}
		}
	}
	
	@Override
	public boolean update() {
		_updateScripts();
		_updateEntities();
		
		return false;
	}
	
	@Override
	public void done() {
		return;
	}
	
	public boolean isLobby() {
		return _isLobby;
	}
	
	public Collection<Entity> getEntities() {
		return _entityMap.values();
	}
	
	public Entity getEntityById(String id) {
		return _entityMap.get(id);
	}
	
	public Script createScriptById(String id) {
		MapObject template = _scriptTemplateMap.get(id);
		return Script.build(template);	
	}
	
	public void startScript(String id) {
		Script script = createScriptById(id);
		script.onStart();
		_activeScripts.add(script);
	}
	
	public void removeEntityById(String id) {
		_entityMap.remove(id);
	}
	
	public void removeEntity(Entity entity) {
		_entityMap.remove(entity.getId());
	}
	
	public void addEntity(Entity entity) {
		_entityMap.put(entity.getId(), entity);
	}
	
	public void addScriptTemplate(MapObject object) {
		_scriptTemplateMap.put(object.getName(), object);
	}

	public boolean entityIdExists(String id) {
		return _entityMap.containsKey(id);
	}
	
	public void addRoomEntranceLocation(String tileMapName, Rectangle location) {
		_roomEntranceLocationMap.put(tileMapName, location);
	}
	
	public String checkForRoomEntrance() {
		Player player = Globals.getGameWorld().getPlayer();
		Rectangle playerRect = new Rectangle(player.getLeft(), player.getTop(), player.getWidth(), player.getHeight());
		for(Entry<String, Rectangle> entry : _roomEntranceLocationMap.entrySet()) {
			String tileMapName = entry.getKey();
			Rectangle roomEntranceRect = entry.getValue();
			
			if(playerRect.overlaps(roomEntranceRect)) {
				return tileMapName;
			}
		}
		
		return null;
	}
	
	private void _updateEntities() {
		Iterator<Entry<String, Entity>> entitiesIter = _entityMap.entrySet().iterator();
		while(entitiesIter.hasNext()) {
			Entity entity = entitiesIter.next().getValue();
			if(entity.update()) {
				entity.done();			
				entitiesIter.remove();
			}
 		}
	}
	
	private void _updateScripts() {
		Iterator<Script> scriptsIter = _activeScripts.iterator();
		while(scriptsIter.hasNext()) {
			Script script = scriptsIter.next();
			if(script.update()) {
				script.done();			
				scriptsIter.remove();
			}
 		}
	}

	public boolean isEntityAt(float left, float bottom, float width, float height) {
		return _isEntityAt(left, bottom, width, height, null, null, null, null);
	}

	public boolean isEntityAt(float left, float bottom, float width, float height, Entity ignoreEntity) {
		return _isEntityAt(left, bottom, width, height, null, null, ignoreEntity, null);
	}
	
	public boolean isEntityAt2(float left, float bottom, float width, float height, Entity includeEntity) {
		Array<Entity> includeEntities = new Array<Entity>();
		includeEntities.add(includeEntity);
		return _isEntityAt(left, bottom, width, height, includeEntities, null, null, null);
	}

	public boolean isEntityAt(float left, float bottom, float width, float height, String ignoreType) {
		return _isEntityAt(left, bottom, width, height, null, null, null, ignoreType);
	}
	
	public boolean isEntityAt(float left, float bottom, float width, float height, Array<String> includeTypes) {
		return _isEntityAt(left, bottom, width, height, null, includeTypes, null, null);
	}
	
	private boolean _isEntityAt(float left, float bottom, float width, float height, Array<Entity> includeEntities, 
			                   Array<String> includeTypes, Entity ignoreEntity, String ignoreType) {
		Rectangle a = new Rectangle(left, bottom, width, height);
		Rectangle b = new Rectangle();
		
		for(Entity entity : getEntities()) {
			if(!entity.isActive() || !entity.isVisible()) {
				continue;
			}
			
			if(ignoreEntity != null && entity.equals(ignoreEntity)) {
				continue;
			}
			
			if(ignoreType != null && entity.getType().equals(ignoreType)) {
				continue;
			}
			
			if(includeEntities != null && !includeEntities.contains(entity, true)) {
				continue;
			}
			
			if(includeTypes != null && !includeTypes.contains(entity.getType(), false)) {
				continue;
			}
			
			b.set(entity.getLeft(), entity.getBottom(), entity.getWidth(), entity.getHeight());
			if(a.overlaps(b)) {
				return true;
			}
		}
			
		return false;
	}
}

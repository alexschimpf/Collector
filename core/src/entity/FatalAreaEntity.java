package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.TimeUtils;

public class FatalAreaEntity extends Entity {

	private final Float _activeDuration;
	private final String _collisionCheck; // overlaps or contains
	private final Vector2 _respawnPos;
	
	private long _lastActiveFlipTime;
	
	public FatalAreaEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);

		if(!Utils.isPropertyEmpty(object, "respawn_pos")) {
			Vector2 respawnOffset = Utils.getPropertyVector2(object, "respawn_pos");
			_respawnPos = respawnOffset.scl(Globals.getTileSize()).add(getCenter());
		} else {
			_respawnPos = null;
		}	
		
		if(!Utils.isPropertyEmpty(object, "active_duration")) {
			_activeDuration = Utils.getPropertyFloat(object, "active_duration");
		} else {
			_activeDuration = null;
		}	
		
		_collisionCheck = Utils.getPropertyString(object, "collision_check");
		
		Fixture fixture = _body.getFixtureList().get(0);
		fixture.setSensor(true);
		
		_lastActiveFlipTime = TimeUtils.millis();
	}
	
	@Override
	public String getType() {
		return "fatal_area";
	}
	
	@Override
	public boolean update() {
		if(_activeDuration != null && TimeUtils.timeSinceMillis(_lastActiveFlipTime) > _activeDuration) {
			_lastActiveFlipTime = TimeUtils.millis();
			setVisible(!_isActive);
			setActive(!_isActive);
		}

		Player player = Globals.getPlayer();
		if(_isActive) {
			if(_collisionCheck.equals("contains") && containsEntity(player)) {
				player.respawn(false, _respawnPos);
			} else if(_collisionCheck.equals("overlaps") && overlapsEntity(player)) {
				player.respawn(true, _respawnPos);
			}
		}

		return super.update();
	}
}

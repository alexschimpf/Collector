package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

public class FatalAreaEntity extends Entity {

	private final String _collisionCheck; // overlaps or contains
	private final Vector2 _respawnPos;
	
	public FatalAreaEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);

		if(!Utils.isPropertyEmpty(object, "respawn_pos")) {
			Vector2 respawnOffset = Utils.getPropertyVector2(object, "respawn_pos");
			_respawnPos = respawnOffset.scl(Globals.getTileSize()).add(getCenter());
		} else {
			_respawnPos = null;
		}	
		
		_collisionCheck = Utils.getPropertyString(object, "collision_check");
		
		Fixture fixture = _body.getFixtureList().get(0);
		fixture.setSensor(true);
	}
	
	@Override
	public String getType() {
		return "fatal_area";
	}
	
	@Override
	public boolean update() {
		Player player = Globals.getPlayer();
		if(_collisionCheck.equals("contains") && getBorderRectangle().contains(player.getBorderRectangle())) {
			player.respawn(false, _respawnPos);
		}
		
		return super.update();
	}

	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		Player player = Globals.getPlayer();
		if(!Utils.isPlayer(entity)) {
			return;
		}

		if(_collisionCheck.equals("overlaps")) {
			player.respawn(true, _respawnPos);
		}
	}
}

package entity;

import misc.Globals;
import misc.IInteractive;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.physics.box2d.Fixture;

public class DoorEntity extends Entity implements IInteractive {

	protected String _exitDoorId;
	
	public DoorEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		_exitDoorId = Utils.getPropertyString(object, "exit_door_id");
		
		Fixture fixture = _body.getFixtureList().get(0);
		fixture.setSensor(true);
	}
	
	@Override
	public String getType() {
		return "door";
	}

	@Override
	public void onInteraction() {
		Player player = Globals.getPlayer();
		if(overlapsEntity(player)) {
			DoorEntity door = (DoorEntity)Globals.getCurrentRoom().getEntityById(_exitDoorId);
			player.setPosition(door.getCenterX(), door.getCenterY());
		}
	}
}

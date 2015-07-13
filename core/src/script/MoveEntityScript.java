package script;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;

import entity.MovingEntity;

public final class MoveEntityScript extends Script {

	private String TARGET_ID;
	
	protected MoveEntityScript(MapObject object) {
		super(object);
		
		TARGET_ID = Utils.getPropertyString(object, "target_id");
	}

	@Override
	public boolean update() {
		return false;
	}
	
	@Override
	public void onStart() {
		MovingEntity entity = (MovingEntity)Globals.getGameWorld().getEntityById(TARGET_ID);
		entity.start();
	}

	@Override
	public String getType() {
		return "move_entity";
	}
}

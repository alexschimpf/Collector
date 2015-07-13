package script;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;

import entity.MovingEntity;

public final class MoveEntityScript extends Script {

	private String TARGET_ID;
	private String[] SERIALIZED_PATH;
	
	protected MoveEntityScript(MapObject object) {
		super(object);
		
		TARGET_ID = Utils.getPropertyString(object, "target_id");
		
		if(Utils.propertyExists(object, "path")) {
			SERIALIZED_PATH = Utils.getPropertyStringArray(object, "path", " ");
		}
	}

	@Override
	public boolean update() {
		return false;
	}
	
	@Override
	public void onStart() {
		MovingEntity entity = (MovingEntity)Globals.getGameWorld().getEntityById(TARGET_ID);
		
		if(SERIALIZED_PATH != null) {
			entity.setPath(SERIALIZED_PATH);
		}
		
		entity.start();
	}

	@Override
	public String getType() {
		return "move_entity";
	}
}

package script;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;

import entity.IMovingEntity;
import entity.SmoothMovingEntity;

public final class MoveEntityScript extends Script {

	private final String TARGET_ID;
	
	private String[] serializedPath;
	private float[] intervals;
	
	protected MoveEntityScript(MapObject object) {
		super(object);
		
		TARGET_ID = Utils.getPropertyString(object, "target_id");
		
		if(Utils.propertyExists(object, "path")) {
			serializedPath = Utils.getPropertyStringArray(object, "path", " ");
		}
		
		if(Utils.propertyExists(object, "intervals")) {
			intervals = Utils.getPropertyFloatArray(object, "intervals", ",");
		}
	}

	@Override
	public boolean update() {
		return false;
	}
	
	@Override
	public void onStart() {
		IMovingEntity entity = (IMovingEntity)Globals.getCurrentRoom().getEntityById(TARGET_ID);
		
		if(serializedPath != null) {
			entity.setPath(serializedPath);
		}
		
		if(intervals != null) {
			entity.setIntervals(intervals);
		}
		
		entity.start();
	}

	@Override
	public String getType() {
		return "move_entity";
	}
}

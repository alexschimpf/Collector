package script;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;

import entity.IMovingEntity;
import entity.SmoothMovingEntity;

public final class MoveEntityScript extends Script {

	private final String _targetId;
	
	private String[] _serializedPath;
	private float[] _intervals;
	
	protected MoveEntityScript(MapObject object) {
		super(object);
		
		_targetId = Utils.getPropertyString(object, "target_id");
		
		if(Utils.propertyExists(object, "path")) {
			_serializedPath = Utils.getPropertyStringArray(object, "path", " ");
		}
		
		if(Utils.propertyExists(object, "intervals")) {
			_intervals = Utils.getPropertyFloatArray(object, "intervals", ",");
		}
	}

	@Override
	public boolean update() {
		return false;
	}
	
	@Override
	public void onStart() {
		IMovingEntity entity = (IMovingEntity)Globals.getCurrentRoom().getEntityById(_targetId);
		
		if(_serializedPath != null) {
			entity.setPath(_serializedPath);
		}
		
		if(_intervals != null) {
			entity.setIntervals(_intervals);
		}
		
		entity.start();
	}

	@Override
	public String getType() {
		return "move_entity";
	}
}
